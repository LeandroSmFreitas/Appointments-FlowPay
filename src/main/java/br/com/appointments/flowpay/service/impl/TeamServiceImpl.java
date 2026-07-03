package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.exceptions.RestNotFound;
import br.com.appointments.flowpay.repository.TeamRepository;
import br.com.appointments.flowpay.service.TeamService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public Team findByName(TeamName name) {
        return teamRepository.findByName(name)
                .orElseThrow(() -> new RestNotFound("Team not found: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findAll() {
        return teamRepository.findAllByOrderByNameAsc();
    }
}
