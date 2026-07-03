package br.com.appointments.flowpay.web.rest;

import br.com.appointments.flowpay.facade.AgentFacade;
import br.com.appointments.flowpay.facade.dto.agent.AgentResponse;
import br.com.appointments.flowpay.facade.dto.agent.AgentStatusUpdateRequest;
import br.com.appointments.flowpay.facade.dto.agent.CreateAgentRequest;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agents")
public class AgentResource {

    private final AgentFacade agentFacade;

    @PostMapping
    @Operation(summary = "Create an agent")
    public ResponseEntity<AgentResponse> create(@Valid @RequestBody CreateAgentRequest request) {
        AgentResponse response = agentFacade.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "List agents")
    public ResponseEntity<List<AgentResponse>> findAll() {
        return ResponseEntity.ok(agentFacade.findAll());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update agent status")
    public ResponseEntity<AgentResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AgentStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(agentFacade.updateStatus(id, request));
    }

    @GetMapping("/{id}/attendances")
    @Operation(summary = "List attendances assigned to an agent")
    public ResponseEntity<List<AttendanceResponse>> findAttendances(@PathVariable UUID id) {
        return ResponseEntity.ok(agentFacade.findAttendances(id));
    }
}
