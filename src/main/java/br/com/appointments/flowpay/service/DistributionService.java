package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.Team;
import java.util.Optional;

public interface DistributionService {

    Attendance distribute(Attendance attendance, Team team);

    Optional<Attendance> distributeNextWaitingToAgent(Agent agent);
}
