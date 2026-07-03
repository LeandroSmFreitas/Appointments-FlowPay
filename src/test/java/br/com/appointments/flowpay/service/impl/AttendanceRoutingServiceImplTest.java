package br.com.appointments.flowpay.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceRoutingServiceImplTest {

    @Mock
    private TeamService teamService;

    private AttendanceRoutingServiceImpl attendanceRoutingService;

    @BeforeEach
    void setUp() {
        attendanceRoutingService = new AttendanceRoutingServiceImpl(teamService);
    }

    @Test
    void shouldRouteCardProblemsToCardsTeam() {
        when(teamService.findByName(TeamName.CARTOES)).thenReturn(team(TeamName.CARTOES));

        Team team = attendanceRoutingService.route("Problemas com cartão");

        assertThat(team.getName()).isEqualTo(TeamName.CARTOES);
    }

    @Test
    void shouldRouteLoanRequestsToLoansTeam() {
        when(teamService.findByName(TeamName.EMPRESTIMOS)).thenReturn(team(TeamName.EMPRESTIMOS));

        Team team = attendanceRoutingService.route("Contratação de empréstimo");

        assertThat(team.getName()).isEqualTo(TeamName.EMPRESTIMOS);
    }

    @Test
    void shouldRouteOtherSubjectsToOthersTeam() {
        when(teamService.findByName(TeamName.OUTROS)).thenReturn(team(TeamName.OUTROS));

        Team team = attendanceRoutingService.route("Atualização cadastral");

        assertThat(team.getName()).isEqualTo(TeamName.OUTROS);
    }

    private Team team(TeamName name) {
        return new Team(name);
    }
}
