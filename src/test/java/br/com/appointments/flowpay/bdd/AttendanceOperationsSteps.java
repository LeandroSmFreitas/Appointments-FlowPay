package br.com.appointments.flowpay.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.TeamService;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import br.com.appointments.flowpay.service.filter.PageableFactory;
import br.com.appointments.flowpay.service.impl.AgentServiceImpl;
import br.com.appointments.flowpay.service.impl.AttendanceRoutingServiceImpl;
import br.com.appointments.flowpay.service.impl.AttendanceServiceImpl;
import br.com.appointments.flowpay.service.impl.DistributionServiceImpl;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class AttendanceOperationsSteps {

    private AgentRepository agentRepository;
    private AttendanceRepository attendanceRepository;
    private TeamService teamService;
    private DashboardEventPublisher dashboardEventPublisher;
    private DistributionServiceImpl distributionService;
    private AttendanceServiceImpl attendanceService;
    private AgentServiceImpl agentService;

    private Team team;
    private Agent agent;
    private Agent createdAgent;
    private Attendance newAttendance;
    private Attendance primaryAttendance;
    private Attendance waitingAttendance;
    private final Queue<Attendance> waitingQueue = new ArrayDeque<>();
    private final List<Attendance> allWaitingAttendances = new ArrayList<>();

    @Before
    public void setUp() {
        agentRepository = mock(AgentRepository.class);
        attendanceRepository = mock(AttendanceRepository.class);
        teamService = mock(TeamService.class);
        dashboardEventPublisher = mock(DashboardEventPublisher.class);

        distributionService = new DistributionServiceImpl(
                attendanceRepository,
                agentRepository,
                dashboardEventPublisher
        );
        attendanceService = new AttendanceServiceImpl(
                attendanceRepository,
                new AttendanceRoutingServiceImpl(teamService),
                distributionService,
                dashboardEventPublisher,
                new PageableFactory()
        );
        agentService = new AgentServiceImpl(
                agentRepository,
                attendanceRepository,
                teamService,
                dashboardEventPublisher,
                distributionService,
                new PageableFactory()
        );

        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> {
            Agent savedAgent = invocation.getArgument(0);
            if (savedAgent.getId() == null) {
                savedAgent.setId(UUID.randomUUID());
            }
            return savedAgent;
        });
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance savedAttendance = invocation.getArgument(0);
            if (savedAttendance.getId() == null) {
                savedAttendance.setId(UUID.randomUUID());
            }
            return savedAttendance;
        });
        when(attendanceRepository.findNextWaitingForUpdate(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(waitingQueue.poll()));
    }

    @Given("an online agent from team {string} with {int} active attendance")
    public void anOnlineAgentFromTeamWithActiveAttendance(String teamName, int activeCount) {
        configureTeam(teamName);
        agent = agent(team, AgentStatus.ONLINE, activeCount);
        when(agentRepository.findAvailableAgentForUpdate(team.getId())).thenReturn(Optional.of(agent));
    }

    @Given("an online agent from team {string} with {int} active attendances")
    public void anOnlineAgentFromTeamWithActiveAttendances(String teamName, int activeCount) {
        configureTeam(teamName);
        agent = agent(team, AgentStatus.ONLINE, activeCount);
        when(agentRepository.findAvailableAgentForUpdate(team.getId())).thenReturn(Optional.empty());
    }

    @Given("no agent has capacity in team {string}")
    public void noAgentHasCapacityInTeam(String teamName) {
        configureTeam(teamName);
        when(agentRepository.findAvailableAgentForUpdate(team.getId())).thenReturn(Optional.empty());
    }

    @When("a new attendance is distributed to team {string}")
    public void aNewAttendanceIsDistributedToTeam(String teamName) {
        configureTeam(teamName);
        newAttendance = attendance("Cliente", "Problemas com cartao", team, AttendanceStatus.WAITING, null);
        newAttendance = distributionService.distribute(newAttendance, team);
    }

    @Then("the attendance status should be {string}")
    public void theAttendanceStatusShouldBe(String status) {
        assertThat(newAttendance.getStatus()).isEqualTo(AttendanceStatus.valueOf(status));
    }

    @Then("the attendance should be assigned to the agent")
    public void theAttendanceShouldBeAssignedToTheAgent() {
        assertThat(newAttendance.getAssignedAgent()).isEqualTo(agent);
    }

    @Then("the attendance should not have an assigned agent")
    public void theAttendanceShouldNotHaveAnAssignedAgent() {
        assertThat(newAttendance.getAssignedAgent()).isNull();
    }

    @Then("the agent active count should be {int}")
    public void theAgentActiveCountShouldBe(int activeCount) {
        assertThat(agent.getActiveCount()).isEqualTo(activeCount);
    }

    @Given("an in progress attendance assigned to an agent with {int} active attendances")
    public void anInProgressAttendanceAssignedToAnAgentWithActiveAttendances(int activeCount) {
        configureTeam("CARTOES");
        agent = agent(team, AgentStatus.ONLINE, activeCount);
        primaryAttendance = attendance(
                "Cliente em atendimento",
                "Problemas com cartao",
                team,
                AttendanceStatus.IN_PROGRESS,
                agent
        );

        when(attendanceRepository.findByIdForUpdate(primaryAttendance.getId()))
                .thenReturn(Optional.of(primaryAttendance));
    }

    @Given("there is a waiting attendance for the same team")
    public void thereIsAWaitingAttendanceForTheSameTeam() {
        waitingAttendance = waitingAttendance(team);
        addWaitingAttendance(waitingAttendance);
    }

    @When("the in progress attendance is finished")
    public void theInProgressAttendanceIsFinished() {
        attendanceService.finish(primaryAttendance.getId());
    }

    @When("the in progress attendance is cancelled")
    public void theInProgressAttendanceIsCancelled() {
        attendanceService.cancel(primaryAttendance.getId());
    }

    @Then("the finished attendance status should be {string}")
    public void theFinishedAttendanceStatusShouldBe(String status) {
        assertThat(primaryAttendance.getStatus()).isEqualTo(AttendanceStatus.valueOf(status));
    }

    @Then("the waiting attendance should become {string}")
    public void theWaitingAttendanceShouldBecome(String status) {
        assertThat(waitingAttendance.getStatus()).isEqualTo(AttendanceStatus.valueOf(status));
        assertThat(waitingAttendance.getAssignedAgent()).isEqualTo(agent);
    }

    @Given("there are {int} waiting attendances for team {string}")
    public void thereAreWaitingAttendancesForTeam(int count, String teamName) {
        configureTeam(teamName);
        for (int i = 0; i < count; i++) {
            addWaitingAttendance(waitingAttendance(team));
        }
    }

    @When("a new agent is created for team {string}")
    public void aNewAgentIsCreatedForTeam(String teamName) {
        configureTeam(teamName);
        Agent newAgent = new Agent();
        newAgent.setName("Novo agente");

        createdAgent = agentService.create(newAgent, TeamName.valueOf(teamName));
        agent = createdAgent;
    }

    @Given("an offline agent from team {string} with {int} active attendances")
    public void anOfflineAgentFromTeamWithActiveAttendances(String teamName, int activeCount) {
        configureTeam(teamName);
        agent = agent(team, AgentStatus.OFFLINE, activeCount);
        createdAgent = agent;
        when(agentRepository.findByIdForUpdate(agent.getId())).thenReturn(Optional.of(agent));
    }

    @When("the agent status is changed to {string}")
    public void theAgentStatusIsChangedTo(String status) {
        createdAgent = agentService.updateStatus(agent.getId(), AgentStatus.valueOf(status));
        agent = createdAgent;
    }

    @Then("the created agent should be {string}")
    public void theCreatedAgentShouldBe(String status) {
        assertThat(createdAgent.getStatus()).isEqualTo(AgentStatus.valueOf(status));
    }

    @Then("the created agent active count should be {int}")
    public void theCreatedAgentActiveCountShouldBe(int activeCount) {
        assertThat(createdAgent.getActiveCount()).isEqualTo(activeCount);
    }

    @Then("{int} waiting attendances should have been distributed to the created agent")
    public void waitingAttendancesShouldHaveBeenDistributedToTheCreatedAgent(int count) {
        long distributedCount = allWaitingAttendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.IN_PROGRESS)
                .filter(attendance -> attendance.getAssignedAgent() == createdAgent)
                .count();

        assertThat(distributedCount).isEqualTo(count);
    }

    private void configureTeam(String teamName) {
        TeamName name = TeamName.valueOf(teamName);
        if (team == null || team.getName() != name) {
            team = new Team(name);
            team.setId(UUID.randomUUID());
        }

        when(teamService.findByName(name)).thenReturn(team);
    }

    private Agent agent(Team agentTeam, AgentStatus status, int activeCount) {
        Agent configuredAgent = new Agent();
        configuredAgent.setId(UUID.randomUUID());
        configuredAgent.setName("Agente");
        configuredAgent.setTeam(agentTeam);
        configuredAgent.setStatus(status);
        configuredAgent.setActiveCount(activeCount);
        return configuredAgent;
    }

    private Attendance waitingAttendance(Team attendanceTeam) {
        return attendance("Cliente aguardando", "Problemas com cartao", attendanceTeam, AttendanceStatus.WAITING, null);
    }

    private Attendance attendance(
            String customerName,
            String subject,
            Team attendanceTeam,
            AttendanceStatus status,
            Agent assignedAgent
    ) {
        Attendance configuredAttendance = new Attendance();
        configuredAttendance.setId(UUID.randomUUID());
        configuredAttendance.setCustomerName(customerName);
        configuredAttendance.setSubject(subject);
        configuredAttendance.setTeam(attendanceTeam);
        configuredAttendance.setStatus(status);
        configuredAttendance.setAssignedAgent(assignedAgent);
        return configuredAttendance;
    }

    private void addWaitingAttendance(Attendance attendance) {
        waitingQueue.add(attendance);
        allWaitingAttendances.add(attendance);
    }
}
