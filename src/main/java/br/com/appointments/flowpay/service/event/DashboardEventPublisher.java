package br.com.appointments.flowpay.service.event;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class DashboardEventPublisher {

    private static final long DASHBOARD_TIMEOUT_MILLIS = Duration.ofMinutes(30).toMillis();

    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(DASHBOARD_TIMEOUT_MILLIS);
        clients.add(emitter);

        emitter.onCompletion(() -> remove(emitter));
        emitter.onTimeout(() -> {
            log.debug("Dashboard SSE connection timed out");
            remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> {
            log.debug("Dashboard SSE connection failed", error);
            remove(emitter);
        });

        sendConnectedEvent(emitter);
        log.debug("Dashboard SSE client connected. clients={}", clients.size());
        return emitter;
    }

    public void publish(DashboardEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    broadcast(event);
                }
            });
            return;
        }

        broadcast(event);
    }

    @Scheduled(fixedDelay = 15000)
    public void sendHeartbeat() {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Removing stale dashboard SSE client during heartbeat", ex);
                remove(emitter);
                emitter.completeWithError(ex);
            }
        }
    }

    private void sendConnectedEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .data(Map.of(
                            "type", "CONNECTED",
                            "createdAt", Instant.now()
                    )));
        } catch (IOException | IllegalStateException ex) {
            log.debug("Could not initialize dashboard SSE client", ex);
            remove(emitter);
            emitter.completeWithError(ex);
        }
    }

    private void broadcast(DashboardEvent event) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.type().name())
                        .data(event));
            } catch (IOException | IllegalStateException ex) {
                log.debug("Removing stale dashboard SSE client", ex);
                remove(emitter);
                emitter.completeWithError(ex);
            }
        }
    }

    private void remove(SseEmitter emitter) {
        clients.remove(emitter);
        log.debug("Dashboard SSE client disconnected. clients={}", clients.size());
    }
}
