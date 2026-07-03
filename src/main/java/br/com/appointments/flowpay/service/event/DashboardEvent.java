package br.com.appointments.flowpay.service.event;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import java.time.Instant;
import java.util.UUID;

public record DashboardEvent(
        DashboardEventType type,
        UUID attendanceId,
        TeamName team,
        UUID agentId,
        Instant createdAt
) {

    public static DashboardEvent attendanceCreated(Attendance attendance) {
        return fromAttendance(DashboardEventType.ATTENDANCE_CREATED, attendance);
    }

    public static DashboardEvent attendanceAssigned(Attendance attendance) {
        return fromAttendance(DashboardEventType.ATTENDANCE_ASSIGNED, attendance);
    }

    public static DashboardEvent attendanceFinished(Attendance attendance) {
        return fromAttendance(DashboardEventType.ATTENDANCE_FINISHED, attendance);
    }

    public static DashboardEvent attendanceCancelled(Attendance attendance) {
        return fromAttendance(DashboardEventType.ATTENDANCE_CANCELLED, attendance);
    }

    public static DashboardEvent agentStatusChanged(Agent agent) {
        return new DashboardEvent(
                DashboardEventType.AGENT_STATUS_CHANGED,
                null,
                agent.getTeam().getName(),
                agent.getId(),
                Instant.now()
        );
    }

    private static DashboardEvent fromAttendance(DashboardEventType type, Attendance attendance) {
        Agent assignedAgent = attendance.getAssignedAgent();
        UUID agentId = assignedAgent == null ? null : assignedAgent.getId();

        return new DashboardEvent(
                type,
                attendance.getId(),
                attendance.getTeam().getName(),
                agentId,
                Instant.now()
        );
    }
}
