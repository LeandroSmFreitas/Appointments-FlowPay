package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Team;

public interface AttendanceRoutingService {

    Team route(String subject);
}
