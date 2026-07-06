package br.com.appointments.flowpay.facade;

import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.facade.dto.PageResponse;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import br.com.appointments.flowpay.facade.dto.attendance.CreateAttendanceRequest;
import br.com.appointments.flowpay.facade.mapper.AttendanceMapper;
import br.com.appointments.flowpay.service.AttendanceService;
import br.com.appointments.flowpay.service.filter.AttendanceSearchFilter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    public PageResponse<AttendanceResponse> search(
            int page,
            int size,
            String sort,
            AttendanceStatus status,
            TeamName team
    ) {
        AttendanceSearchFilter filter = new AttendanceSearchFilter(page, size, sort, status, team);
        Page<AttendanceResponse> responsePage = attendanceService.search(filter).map(attendanceMapper::toDto);

        return PageResponse.from(responsePage);
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
