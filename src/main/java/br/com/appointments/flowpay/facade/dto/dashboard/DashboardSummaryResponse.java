package br.com.appointments.flowpay.facade.dto.dashboard;

import java.util.List;

public record DashboardSummaryResponse(
        long totalWaiting,
        long totalInProgress,
        long totalFinishedToday,
        List<DashboardTeamSummaryResponse> teams
) {
}
