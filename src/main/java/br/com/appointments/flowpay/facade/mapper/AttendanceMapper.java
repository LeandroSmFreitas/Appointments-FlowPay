package br.com.appointments.flowpay.facade.mapper;

import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import br.com.appointments.flowpay.facade.dto.attendance.CreateAttendanceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper extends EntityMapper<AttendanceResponse, Attendance> {

    @Override
    @Mapping(target = "team", source = "team.name")
    @Mapping(target = "assignedAgentId", source = "assignedAgent.id")
    @Mapping(target = "assignedAgentName", source = "assignedAgent.name")
    AttendanceResponse toDto(Attendance entity);

    @Override
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    Attendance toEntity(AttendanceResponse dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "assignedAgent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attendance toEntity(CreateAttendanceRequest request);
}
