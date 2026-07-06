package br.com.appointments.flowpay.web.rest;

import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.facade.AttendanceFacade;
import br.com.appointments.flowpay.facade.dto.PageResponse;
import br.com.appointments.flowpay.facade.dto.attendance.AttendanceResponse;
import br.com.appointments.flowpay.facade.dto.attendance.CreateAttendanceRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendances")
public class AttendanceResource {

    private final AttendanceFacade attendanceFacade;

    @PostMapping
    @Operation(summary = "Create and distribute an attendance")
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody CreateAttendanceRequest request) {
        AttendanceResponse response = attendanceFacade.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "List attendances")
    public ResponseEntity<PageResponse<AttendanceResponse>> search(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) TeamName team
    ) {
        return ResponseEntity.ok(attendanceFacade.search(page, size, sort, status, team));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find attendance by id")
    public ResponseEntity<AttendanceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceFacade.findById(id));
    }

    @PatchMapping("/{id}/finish")
    @Operation(summary = "Finish an attendance and distribute the next waiting one")
    public ResponseEntity<AttendanceResponse> finish(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceFacade.finish(id));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an attendance")
    public ResponseEntity<AttendanceResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceFacade.cancel(id));
    }
}
