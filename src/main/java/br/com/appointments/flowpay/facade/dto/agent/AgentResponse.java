package br.com.appointments.flowpay.facade.dto.agent;

import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import java.time.Instant;
import java.util.UUID;

public record AgentResponse(
        UUID id,
        String name,
        TeamName team,
        AgentStatus status,
        int activeCount,
        Instant lastAssignedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
