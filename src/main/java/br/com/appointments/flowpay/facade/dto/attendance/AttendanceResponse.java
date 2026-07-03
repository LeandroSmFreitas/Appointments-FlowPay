package br.com.appointments.flowpay.facade.dto.attendance;

import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import java.time.Instant;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        String customerName,
        String subject,
        TeamName team,
        AttendanceStatus status,
        UUID assignedAgentId,
        String assignedAgentName,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        Instant updatedAt
) {
}
