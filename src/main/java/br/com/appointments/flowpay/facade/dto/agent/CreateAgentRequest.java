package br.com.appointments.flowpay.facade.dto.agent;

import br.com.appointments.flowpay.domain.enumeration.TeamName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAgentRequest(
        @NotBlank String name,
        @NotNull TeamName team
) {
}
