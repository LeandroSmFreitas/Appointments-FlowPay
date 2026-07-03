package br.com.appointments.flowpay.facade.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record CreateAttendanceRequest(
        @NotBlank String customerName,
        @NotBlank String subject
) {
}
