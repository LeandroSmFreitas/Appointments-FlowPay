package br.com.appointments.flowpay.service.impl;

import br.com.appointments.flowpay.domain.Team;
import br.com.appointments.flowpay.domain.enumeration.TeamName;
import br.com.appointments.flowpay.exceptions.RestBusinessException;
import br.com.appointments.flowpay.service.AttendanceRoutingService;
import br.com.appointments.flowpay.service.TeamService;
import java.text.Normalizer;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AttendanceRoutingServiceImpl implements AttendanceRoutingService {

    private final TeamService teamService;

    @Override
    public Team route(String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new RestBusinessException("Subject is required");
        }

        String normalizedSubject = normalize(subject);

        if (normalizedSubject.contains("cartao")) {
            return teamService.findByName(TeamName.CARTOES);
        }

        if (normalizedSubject.contains("emprestimo")) {
            return teamService.findByName(TeamName.EMPRESTIMOS);
        }

        return teamService.findByName(TeamName.OUTROS);
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
