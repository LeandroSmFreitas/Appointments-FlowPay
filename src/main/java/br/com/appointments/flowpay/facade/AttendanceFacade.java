package br.com.appointments.flowpay.facade;

import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import br.com.appointments.flowpay.facade.dto.attendance.CreateAttendanceRequest;
import br.com.appointments.flowpay.facade.mapper.AttendanceMapper;
import br.com.appointments.flowpay.service.AttendanceService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceFacade {

    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceResponse create(CreateAttendanceRequest request) {
        Attendance attendance = attendanceMapper.toEntity(request);
        return attendanceMapper.toDto(attendanceService.create(attendance));
    }

    public List<AttendanceResponse> findAll() {
        return attendanceMapper.toDto(attendanceService.findAll());
    }

    public AttendanceResponse findById(UUID id) {
        return attendanceMapper.toDto(attendanceService.findById(id));
    }

    public AttendanceResponse finish(UUID id) {
        return attendanceMapper.toDto(attendanceService.finish(id));
    }

    public AttendanceResponse cancel(UUID id) {
        return attendanceMapper.toDto(attendanceService.cancel(id));
    }
}
