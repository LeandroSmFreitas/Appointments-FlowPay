package br.com.appointments.flowpay.web.rest;

import br.com.appointments.flowpay.facade.DashboardFacade;
import br.com.appointments.flowpay.facade.dto.dashboard.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardResource {

    private final DashboardFacade dashboardFacade;

    @GetMapping("/summary")
    @Operation(summary = "Get attendance distribution summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardFacade.getSummary());
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to dashboard update events")
    public SseEmitter subscribeToEvents() {
        return dashboardFacade.subscribeToEvents();
    }
}
