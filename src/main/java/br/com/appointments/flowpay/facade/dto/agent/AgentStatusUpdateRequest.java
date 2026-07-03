package br.com.appointments.flowpay.facade.dto.agent;

import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import jakarta.validation.constraints.NotNull;

public record AgentStatusUpdateRequest(@NotNull AgentStatus status) {
}
