package br.com.appointments.flowpay.facade;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.facade.dto.agent.AgentResponse;
import br.com.appointments.flowpay.facade.dto.agent.AgentStatusUpdateRequest;
import br.com.appointments.flowpay.facade.dto.agent.CreateAgentRequest;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import br.com.appointments.flowpay.facade.mapper.AgentMapper;
import br.com.appointments.flowpay.facade.mapper.AttendanceMapper;
import br.com.appointments.flowpay.service.AgentService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentFacade {

    private final AgentService agentService;
    private final AgentMapper agentMapper;
    private final AttendanceMapper attendanceMapper;

    public AgentResponse create(CreateAgentRequest request) {
        Agent agent = agentMapper.toEntity(request);
        return agentMapper.toDto(agentService.create(agent, request.team()));
    }

    public List<AgentResponse> findAll() {
        return agentMapper.toDto(agentService.findAll());
    }

    public AgentResponse updateStatus(UUID id, AgentStatusUpdateRequest request) {
        return agentMapper.toDto(agentService.updateStatus(id, request.status()));
    }

    public List<AttendanceResponse> findAttendances(UUID id) {
        return attendanceMapper.toDto(agentService.findAttendances(id));
    }
}
