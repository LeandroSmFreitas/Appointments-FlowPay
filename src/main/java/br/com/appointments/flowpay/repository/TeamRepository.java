package br.com.appointments.flowpay.repository;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByName(TeamName name);

    List<Team> findAllByOrderByNameAsc();
}
