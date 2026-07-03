package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.exceptions.RestBusinessException;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.DistributionService;
import br.com.appointments.flowpay.service.event.DashboardEvent;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionServiceImpl implements DistributionService {

    private static final int MAX_ACTIVE_ATTENDANCES = 3;

    private final AttendanceRepository attendanceRepository;
    private final AgentRepository agentRepository;
    private final DashboardEventPublisher dashboardEventPublisher;

    @Override
    @Transactional
    public Attendance distribute(Attendance attendance, Team team) {
        Instant now = Instant.now();
        attendance.setTeam(team);

        // The repository query uses FOR UPDATE SKIP LOCKED. A concurrent request
        // cannot update the same agent row, and locked candidates are skipped
        // instead of blocking the whole queue, keeping active_count <= 3.
        Optional<Agent> availableAgent = agentRepository.findAvailableAgentForUpdate(team.getId());

        if (availableAgent.isPresent()) {
            Agent agent = availableAgent.get();
            assign(attendance, agent, now);
            agentRepository.save(agent);
            log.info("Attendance assigned to agent {} in team {}", agent.getId(), team.getName());
        } else {
            markAsWaiting(attendance);
            log.info("Attendance queued for team {}", team.getName());
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        dashboardEventPublisher.publish(DashboardEvent.attendanceCreated(savedAttendance));

        if (savedAttendance.getStatus() == AttendanceStatus.IN_PROGRESS) {
            dashboardEventPublisher.publish(DashboardEvent.attendanceAssigned(savedAttendance));
        }

        return savedAttendance;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Attendance> distributeNextWaitingToAgent(Agent agent) {
        if (agent.getStatus() != AgentStatus.ONLINE || agent.getActiveCount() >= MAX_ACTIVE_ATTENDANCES) {
            return Optional.empty();
        }

        Instant now = Instant.now();

        return attendanceRepository.findNextWaitingForUpdate(agent.getTeam().getId())
                .map(waitingAttendance -> {
                    assign(waitingAttendance, agent, now);
                    agentRepository.save(agent);
                    Attendance savedAttendance = attendanceRepository.save(waitingAttendance);
                    dashboardEventPublisher.publish(DashboardEvent.attendanceAssigned(savedAttendance));
                    log.info("Waiting attendance {} assigned to agent {}", savedAttendance.getId(), agent.getId());
                    return savedAttendance;
                });
    }

    private void assign(Attendance attendance, Agent agent, Instant now) {
        if (agent.getActiveCount() >= MAX_ACTIVE_ATTENDANCES) {
            throw new RestBusinessException("Agent has reached the limit of active attendances");
        }

        attendance.setStatus(AttendanceStatus.IN_PROGRESS);
        attendance.setAssignedAgent(agent);
        attendance.setStartedAt(now);
        attendance.setFinishedAt(null);

        agent.setActiveCount(agent.getActiveCount() + 1);
        agent.setLastAssignedAt(now);
    }

    private void markAsWaiting(Attendance attendance) {
        attendance.setStatus(AttendanceStatus.WAITING);
        attendance.setAssignedAgent(null);
        attendance.setStartedAt(null);
        attendance.setFinishedAt(null);
    }
}
