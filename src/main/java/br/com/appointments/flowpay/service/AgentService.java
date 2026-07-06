package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.service.filter.AgentSearchFilter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface AgentService {

    Agent create(Agent agent, TeamName teamName);

    Page<Agent> search(AgentSearchFilter filter);

    Agent findById(UUID id);

    Agent updateStatus(UUID id, AgentStatus status);

    List<Attendance> findAttendances(UUID id);
}
