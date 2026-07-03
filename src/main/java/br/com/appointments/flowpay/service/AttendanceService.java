package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Attendance;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    Attendance create(Attendance attendance);

    List<Attendance> findAll();

    Attendance findById(UUID id);

    Attendance finish(UUID id);

    Attendance cancel(UUID id);
}
