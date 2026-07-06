package br.com.appointments.flowpay.service;

import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.service.filter.AttendanceSearchFilter;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface AttendanceService {

    Attendance create(Attendance attendance);

    Page<Attendance> search(AttendanceSearchFilter filter);

    Attendance findById(UUID id);

    Attendance finish(UUID id);

    Attendance cancel(UUID id);
}
