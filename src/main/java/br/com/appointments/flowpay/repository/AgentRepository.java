package br.com.appointments.flowpay.repository;

import br.com.appointments.flowpay.domain.Agent;
import br.com.appointments.flowpay.domain.enumeration.AgentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AgentRepository extends JpaRepository<Agent, UUID> {

    @EntityGraph(attributePaths = "team")
    List<Agent> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = "team")
    Optional<Agent> findOneWithTeamById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agent a join fetch a.team where a.id = :id")
    Optional<Agent> findByIdForUpdate(@Param("id") UUID id);

    long countByTeamIdAndStatus(UUID teamId, AgentStatus status);

    @Query("select coalesce(sum(a.activeCount), 0) from Agent a where a.team.id = :teamId")
    long sumActiveCountByTeamId(@Param("teamId") UUID teamId);

    /**
     * FOR UPDATE locks the chosen agent row before active_count is incremented.
     * SKIP LOCKED makes concurrent transactions ignore rows already selected by
     * another request, preventing two requests from consuming the same capacity slot.
     */
    @Query(value = """
            SELECT *
            FROM agents
            WHERE team_id = :teamId
              AND status = 'ONLINE'
              AND active_count < 3
            ORDER BY active_count ASC,
                     last_assigned_at ASC NULLS FIRST
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<Agent> findAvailableAgentForUpdate(@Param("teamId") UUID teamId);
}
