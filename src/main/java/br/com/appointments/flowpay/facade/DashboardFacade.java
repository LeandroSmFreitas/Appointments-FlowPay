package br.com.appointments.flowpay.facade;

import br.com.appointments.flowpay.facade.dto.dashboard.DashboardSummaryResponse;
import br.com.appointments.flowpay.service.DashboardService;
import br.com.appointments.flowpay.service.event.DashboardEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class DashboardFacade {

    private final DashboardService dashboardService;
    private final DashboardEventPublisher dashboardEventPublisher;

    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    public SseEmitter subscribeToEvents() {
        return dashboardEventPublisher.register();
    }
}
