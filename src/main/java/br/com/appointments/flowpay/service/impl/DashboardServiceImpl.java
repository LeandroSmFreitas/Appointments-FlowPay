package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.facade.dto.dashboard.DashboardSummaryResponse;
import br.com.appointments.flowpay.facade.dto.dashboard.DashboardTeamSummaryResponse;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.DashboardService;
import br.com.appointments.flowpay.service.TeamService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int MAX_ACTIVE_ATTENDANCES = 3;

    private final AttendanceRepository attendanceRepository;
    private final AgentRepository agentRepository;
    private final TeamService teamService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        DateRange today = todayRange();

        long totalWaiting = attendanceRepository.countByStatus(AttendanceStatus.WAITING);
        long totalInProgress = attendanceRepository.countByStatus(AttendanceStatus.IN_PROGRESS);
        long totalFinishedToday = attendanceRepository.countByStatusAndFinishedAtBetween(
                AttendanceStatus.FINISHED,
                today.start(),
                today.end()
        );

        List<DashboardTeamSummaryResponse> teams = teamService.findAll()
                .stream()
                .map(team -> buildTeamSummary(team, today))
                .toList();

        return new DashboardSummaryResponse(totalWaiting, totalInProgress, totalFinishedToday, teams);
    }

    private DashboardTeamSummaryResponse buildTeamSummary(Team team, DateRange today) {
        long waiting = attendanceRepository.countByTeamIdAndStatus(team.getId(), AttendanceStatus.WAITING);
        long inProgress = attendanceRepository.countByTeamIdAndStatus(team.getId(), AttendanceStatus.IN_PROGRESS);
        long finishedToday = attendanceRepository.countByTeamIdAndStatusAndFinishedAtBetween(
                team.getId(),
                AttendanceStatus.FINISHED,
                today.start(),
                today.end()
        );
        long agentsOnline = agentRepository.countByTeamIdAndStatus(team.getId(), AgentStatus.ONLINE);
        long totalCapacity = agentsOnline * MAX_ACTIVE_ATTENDANCES;
        long usedCapacity = agentRepository.sumActiveCountByTeamId(team.getId());
        long availableCapacity = Math.max(totalCapacity - usedCapacity, 0);

        return new DashboardTeamSummaryResponse(
                team.getName(),
                waiting,
                inProgress,
                finishedToday,
                agentsOnline,
                totalCapacity,
                usedCapacity,
                availableCapacity
        );
    }

    private DateRange todayRange() {
        ZoneId zone = ZoneId.systemDefault();
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        return new DateRange(start, end);
    }

    private record DateRange(Instant start, Instant end) {
    }
}
