package br.com.appointments.flowpay.facade.mapper;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.facade.dto.agent.AgentResponse;
import br.com.appointments.flowpay.facade.dto.agent.CreateAgentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper extends EntityMapper<AgentResponse, Agent> {

    @Override
    @Mapping(target = "team", source = "team.name")
    AgentResponse toDto(Agent entity);

    @Override
    @Mapping(target = "team", ignore = true)
    Agent toEntity(AgentResponse dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "activeCount", ignore = true)
    @Mapping(target = "lastAssignedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Agent toEntity(CreateAgentRequest request);
}
