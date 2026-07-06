package br.com.appointments.flowpay.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.service.TeamService;
import br.com.appointments.flowpay.service.impl.AttendanceRoutingServiceImpl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import java.util.List;

public class AttendanceRoutingSteps {

    private AttendanceRoutingServiceImpl attendanceRoutingService;
    private Team routedTeam;

    @Given("the FlowPay teams are available")
    public void theFlowPayTeamsAreAvailable() {
        attendanceRoutingService = new AttendanceRoutingServiceImpl(new InMemoryTeamService());
    }

    @When("the customer informs the subject {string}")
    public void theCustomerInformsTheSubject(String subject) {
        routedTeam = attendanceRoutingService.route(subject);
    }

    @Then("the attendance should be routed to {string}")
    public void theAttendanceShouldBeRoutedTo(String teamName) {
        assertThat(routedTeam.getName()).isEqualTo(TeamName.valueOf(teamName));
    }

    private static class InMemoryTeamService implements TeamService {

        @Override
        public Team findByName(TeamName name) {
            return new Team(name);
        }

        @Override
        public List<Team> findAll() {
            return Arrays.stream(TeamName.values())
                    .map(Team::new)
                    .toList();
        }
    }
}
