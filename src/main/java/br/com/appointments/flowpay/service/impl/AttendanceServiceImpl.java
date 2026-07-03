package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.exceptions.RestBusinessException;
import br.com.appointments.flowpay.exceptions.RestNotFound;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.AttendanceRoutingService;
import br.com.appointments.flowpay.service.AttendanceService;
import br.com.appointments.flowpay.service.DistributionService;
import br.com.appointments.flowpay.service.event.DashboardEvent;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceRoutingService attendanceRoutingService;
    private final DistributionService distributionService;
    private final DashboardEventPublisher dashboardEventPublisher;

    @Override
    @Transactional
    public Attendance create(Attendance attendance) {
        Team team = attendanceRoutingService.route(attendance.getSubject());
        return distributionService.distribute(attendance, team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findAll() {
        return attendanceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Attendance findById(UUID id) {
        return attendanceRepository.findOneWithRelationsById(id)
                .orElseThrow(() -> new RestNotFound("Attendance not found: " + id));
    }

    @Override
    @Transactional
    public Attendance finish(UUID id) {
        Attendance attendance = attendanceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RestNotFound("Attendance not found: " + id));

        if (attendance.getStatus() != AttendanceStatus.IN_PROGRESS) {
            throw new RestBusinessException("Only in-progress attendances can be finished");
        }

        Agent agent = requireAssignedAgent(attendance);
        finishAttendance(attendance);
        decrementActiveCount(agent);
        dashboardEventPublisher.publish(DashboardEvent.attendanceFinished(attendance));

        // The same transaction frees the agent capacity and locks the next
        // WAITING row, so another finish request cannot assign this same customer.
        distributionService.distributeNextWaitingToAgent(agent);

        log.info("Attendance {} finished", id);
        return attendance;
    }

    @Override
    @Transactional
    public Attendance cancel(UUID id) {
        Attendance attendance = attendanceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RestNotFound("Attendance not found: " + id));

        if (attendance.getStatus() == AttendanceStatus.FINISHED
                || attendance.getStatus() == AttendanceStatus.CANCELLED) {
            throw new RestBusinessException("Attendance is already closed");
        }

        Agent releasedAgent = null;
        if (attendance.getStatus() == AttendanceStatus.IN_PROGRESS) {
            releasedAgent = requireAssignedAgent(attendance);
            decrementActiveCount(releasedAgent);
        }

        attendance.setStatus(AttendanceStatus.CANCELLED);
        attendance.setFinishedAt(Instant.now());
        dashboardEventPublisher.publish(DashboardEvent.attendanceCancelled(attendance));

        if (releasedAgent != null) {
            distributionService.distributeNextWaitingToAgent(releasedAgent);
        }

        log.info("Attendance {} cancelled", id);
        return attendance;
    }

    private void finishAttendance(Attendance attendance) {
        attendance.setStatus(AttendanceStatus.FINISHED);
        attendance.setFinishedAt(Instant.now());
    }

    private Agent requireAssignedAgent(Attendance attendance) {
        Agent agent = attendance.getAssignedAgent();
        if (agent == null) {
            throw new RestBusinessException("Attendance has no assigned agent");
        }
        return agent;
    }

    private void decrementActiveCount(Agent agent) {
        if (agent.getActiveCount() <= 0) {
            throw new RestBusinessException("Agent active attendance counter is inconsistent");
        }

        agent.setActiveCount(agent.getActiveCount() - 1);
    }
}
