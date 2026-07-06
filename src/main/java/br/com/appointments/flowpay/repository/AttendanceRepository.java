package br.com.appointments.flowpay.repository;

import br.com.appointments.flowpay.domain.Attendance;
import br.com.appointments.flowpay.domain.enumeration.AttendanceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    @Override
    @EntityGraph(attributePaths = {"team", "assignedAgent"})
    Page<Attendance> findAll(Specification<Attendance> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"team", "assignedAgent"})
    List<Attendance> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"team", "assignedAgent"})
    Optional<Attendance> findOneWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"team", "assignedAgent"})
    List<Attendance> findAllByAssignedAgentIdOrderByCreatedAtDesc(UUID agentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select at
            from Attendance at
            join fetch at.team
            left join fetch at.assignedAgent ag
            left join fetch ag.team
            where at.id = :id
            """)
    Optional<Attendance> findByIdForUpdate(@Param("id") UUID id);

    long countByStatus(AttendanceStatus status);

    long countByStatusAndFinishedAtBetween(AttendanceStatus status, Instant start, Instant end);

    long countByTeamIdAndStatus(UUID teamId, AttendanceStatus status);

    long countByTeamIdAndStatusAndFinishedAtBetween(
            UUID teamId,
            AttendanceStatus status,
            Instant start,
            Instant end
    );

    /**
     * The waiting attendance row is locked before reassignment. SKIP LOCKED keeps
     * parallel finish requests from selecting the same waiting attendance.
     */
    @Query(value = """
            SELECT *
            FROM attendances
            WHERE team_id = :teamId
              AND status = 'WAITING'
            ORDER BY created_at
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<Attendance> findNextWaitingForUpdate(@Param("teamId") UUID teamId);
}
