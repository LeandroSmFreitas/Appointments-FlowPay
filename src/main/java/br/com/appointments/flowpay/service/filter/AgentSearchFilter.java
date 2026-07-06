package br.com.appointments.flowpay.service.filter;

import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;

public record AgentSearchFilter(
        int page,
        int size,
        String sort,
        AgentStatus status,
        TeamName team
) {
}
