package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.exceptions.RestNotFound;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.AgentService;
import br.com.appointments.flowpay.service.DistributionService;
import br.com.appointments.flowpay.service.TeamService;
import br.com.appointments.flowpay.service.event.DashboardEvent;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentServiceImpl implements AgentService {

    private static final int MAX_ACTIVE_ATTENDANCES = 3;

    private final AgentRepository agentRepository;
    private final AttendanceRepository attendanceRepository;
    private final TeamService teamService;
    private final DashboardEventPublisher dashboardEventPublisher;
    private final DistributionService distributionService;

    @Override
    @Transactional
    public Agent create(Agent agent, TeamName teamName) {
        Team team = teamService.findByName(teamName);

        agent.setTeam(team);
        agent.setStatus(AgentStatus.ONLINE);
        agent.setActiveCount(0);

        Agent savedAgent = agentRepository.save(agent);
        log.info("Agent {} created for team {}", savedAgent.getId(), team.getName());
        return savedAgent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agent> findAll() {
        return agentRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Agent findById(UUID id) {
        return agentRepository.findOneWithTeamById(id)
                .orElseThrow(() -> new RestNotFound("Agent not found: " + id));
    }

    @Override
    @Transactional
    public Agent updateStatus(UUID id, AgentStatus status) {
        Agent agent = agentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RestNotFound("Agent not found: " + id));

        agent.setStatus(status);
        dashboardEventPublisher.publish(DashboardEvent.agentStatusChanged(agent));

        if (status == AgentStatus.ONLINE) {
            distributeWaitingAttendances(agent);
        }

        log.info("Agent {} status changed to {}", id, status);
        return agent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findAttendances(UUID id) {
        if (!agentRepository.existsById(id)) {
            throw new RestNotFound("Agent not found: " + id);
        }

        return attendanceRepository.findAllByAssignedAgentIdOrderByCreatedAtDesc(id);
    }

    private void distributeWaitingAttendances(Agent agent) {
        int assignedCount = 0;

        while (agent.getActiveCount() < MAX_ACTIVE_ATTENDANCES
                && distributionService.distributeNextWaitingToAgent(agent).isPresent()) {
            assignedCount++;
        }

        if (assignedCount > 0) {
            log.info("Agent {} received {} waiting attendances after going online", agent.getId(), assignedCount);
        }
    }
}
