package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import java.util.List;

public interface TeamService {

    Team findByName(TeamName name);

    List<Team> findAll();
}
