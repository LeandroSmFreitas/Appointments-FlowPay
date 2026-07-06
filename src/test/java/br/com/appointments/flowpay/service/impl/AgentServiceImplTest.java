package br.com.appointments.flowpay.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.DistributionService;
import br.com.appointments.flowpay.service.TeamService;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import br.com.appointments.flowpay.service.filter.PageableFactory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgentServiceImplTest {

    private final AgentRepository agentRepository = mock(AgentRepository.class);
    private final AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
    private final TeamService teamService = mock(TeamService.class);
    private final DashboardEventPublisher dashboardEventPublisher = mock(DashboardEventPublisher.class);
    private final DistributionService distributionService = mock(DistributionService.class);
    private final PageableFactory pageableFactory = new PageableFactory();

    private final AgentServiceImpl agentService = new AgentServiceImpl(
            agentRepository,
            attendanceRepository,
            teamService,
            dashboardEventPublisher,
            distributionService,
            pageableFactory
    );

    @Test
    void shouldDistributeWaitingAttendancesWhenAgentIsCreated() {
        Team team = new Team(TeamName.CARTOES);
        Agent agent = new Agent();
        agent.setName("Ana Souza");

        when(teamService.findByName(TeamName.CARTOES)).thenReturn(team);
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> {
            Agent savedAgent = invocation.getArgument(0);
            savedAgent.setId(UUID.randomUUID());
            return savedAgent;
        });
        when(distributionService.distributeNextWaitingToAgent(any(Agent.class))).thenAnswer(invocation -> {
            Agent savedAgent = invocation.getArgument(0);

            if (savedAgent.getActiveCount() < 2) {
                savedAgent.setActiveCount(savedAgent.getActiveCount() + 1);
                return Optional.of(new Attendance());
            }

            return Optional.empty();
        });

        Agent createdAgent = agentService.create(agent, TeamName.CARTOES);

        assertThat(createdAgent.getStatus()).isEqualTo(AgentStatus.ONLINE);
        assertThat(createdAgent.getActiveCount()).isEqualTo(2);
        verify(distributionService, times(3)).distributeNextWaitingToAgent(createdAgent);
    }
}
