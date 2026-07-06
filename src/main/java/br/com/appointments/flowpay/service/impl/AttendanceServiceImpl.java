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
import br.com.appointments.flowpay.service.filter.AttendanceSearchFilter;
import br.com.appointments.flowpay.service.filter.PageableFactory;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "createdAt", "createdAt",
            "customerName", "customerName",
            "status", "status",
            "team", "team.name",
            "assignedAgentName", "assignedAgent.name"
    );

    private final AttendanceRepository attendanceRepository;
    private final AttendanceRoutingService attendanceRoutingService;
    private final DistributionService distributionService;
    private final DashboardEventPublisher dashboardEventPublisher;
    private final PageableFactory pageableFactory;

    @Override
    @Transactional
    public Attendance create(Attendance attendance) {
        Team team = attendanceRoutingService.route(attendance.getSubject());
        return distributionService.distribute(attendance, team);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Attendance> search(AttendanceSearchFilter filter) {
        Pageable pageable = pageableFactory.create(
                filter.page(),
                filter.size(),
                filter.sort(),
                "createdAt",
                Sort.Direction.DESC,
                ALLOWED_SORTS
        );

        return attendanceRepository.findAll(buildSpecification(filter), pageable);
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

    private Specification<Attendance> buildSpecification(AttendanceSearchFilter filter) {
        Specification<Attendance> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (filter.status() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), filter.status()));
        }

        if (filter.team() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("team").get("name"), filter.team()));
        }

        return specification;
    }
}
