package br.com.appointments.flowpay.facade.dto.dashboard;

import br.com.appointments.flowpay.domain.enumeration.TeamName;

public record DashboardTeamSummaryResponse(
        TeamName team,
        long waiting,
        long inProgress,
        long finishedToday,
        long agentsOnline,
        long totalCapacity,
        long usedCapacity,
        long availableCapacity
) {
}
