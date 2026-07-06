package br.com.appointments.flowpay.service.filter;

import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;

public record AttendanceSearchFilter(
        int page,
        int size,
        String sort,
        AttendanceStatus status,
        TeamName team
) {
}
