package br.com.appointments.flowpay;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.repository.AgentRepository;
import br.com.appointments.flowpay.repository.AttendanceRepository;
import br.com.appointments.flowpay.service.AgentService;
import br.com.appointments.flowpay.service.AttendanceService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AttendanceFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AgentService agentService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        attendanceRepository.deleteAll();
        agentRepository.deleteAll();
    }

    @Test
    void shouldDistributeAttendanceWhenAgentIsAvailable() {
        Agent agent = createAgent("Ana", TeamName.CARTOES);

        Attendance attendance = createAttendance("Cliente 1", "Problemas com cartao");

        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.IN_PROGRESS);
        assertThat(attendance.getAssignedAgent().getId()).isEqualTo(agent.getId());
        assertThat(reloadAgent(agent.getId()).getActiveCount()).isEqualTo(1);
    }

    @Test
    void shouldCreateWaitingAttendanceWhenAllAgentsAreFull() {
        Agent agent = createAgent("Bia", TeamName.CARTOES);
        createAttendance("Cliente 1", "Problemas com cartao");
        createAttendance("Cliente 2", "Problemas com cartao");
        createAttendance("Cliente 3", "Problemas com cartao");

        Attendance waiting = createAttendance("Cliente 4", "Problemas com cartao");

        assertThat(waiting.getStatus()).isEqualTo(AttendanceStatus.WAITING);
        assertThat(waiting.getAssignedAgent()).isNull();
        assertThat(reloadAgent(agent.getId()).getActiveCount()).isEqualTo(3);
    }

    @Test
    void shouldFinishAttendanceAndDistributeNextWaitingAutomatically() {
        Agent agent = createAgent("Caio", TeamName.CARTOES);
        Attendance first = createAttendance("Cliente 1", "Problemas com cartao");
        createAttendance("Cliente 2", "Problemas com cartao");
        createAttendance("Cliente 3", "Problemas com cartao");
        Attendance waiting = createAttendance("Cliente 4", "Problemas com cartao");

        Attendance finished = attendanceService.finish(first.getId());
        Attendance reassigned = attendanceService.findById(waiting.getId());

        assertThat(finished.getStatus()).isEqualTo(AttendanceStatus.FINISHED);
        assertThat(reassigned.getStatus()).isEqualTo(AttendanceStatus.IN_PROGRESS);
        assertThat(reassigned.getAssignedAgent().getId()).isEqualTo(agent.getId());
        assertThat(reloadAgent(agent.getId()).getActiveCount()).isEqualTo(3);
    }

    @Test
    void shouldNeverAllowAgentActiveCountAboveThree() {
        Agent agent = createAgent("Duda", TeamName.CARTOES);

        createAttendance("Cliente 1", "Problemas com cartao");
        createAttendance("Cliente 2", "Problemas com cartao");
        createAttendance("Cliente 3", "Problemas com cartao");
        createAttendance("Cliente 4", "Problemas com cartao");
        createAttendance("Cliente 5", "Problemas com cartao");

        assertThat(reloadAgent(agent.getId()).getActiveCount()).isEqualTo(3);
        assertThat(attendanceRepository.countByStatus(AttendanceStatus.IN_PROGRESS)).isEqualTo(3);
        assertThat(attendanceRepository.countByStatus(AttendanceStatus.WAITING)).isEqualTo(2);
    }

    private Agent createAgent(String name, TeamName teamName) {
        Agent agent = new Agent();
        agent.setName(name);
        return agentService.create(agent, teamName);
    }

    private Attendance createAttendance(String customerName, String subject) {
        Attendance attendance = new Attendance();
        attendance.setCustomerName(customerName);
        attendance.setSubject(subject);
        return attendanceService.create(attendance);
    }

    private Agent reloadAgent(UUID id) {
        return agentRepository.findOneWithTeamById(id).orElseThrow();
    }
}
