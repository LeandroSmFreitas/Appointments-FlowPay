package br.com.appointments.flowpay.service.filter;

import br.com.appointments.flowpay.exceptions.RestBusinessException;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PageableFactory {

    private static final int MAX_PAGE_SIZE = 100;

    public Pageable create(
            int page,
            int size,
            String sort,
            String defaultSort,
            Sort.Direction defaultDirection,
            Map<String, String> allowedSorts
    ) {
        validatePage(page);
        validateSize(size);

        Sort parsedSort = parseSort(sort, defaultSort, defaultDirection, allowedSorts);
        return PageRequest.of(page, size, parsedSort);
    }

    private void validatePage(int page) {
        if (page < 0) {
            throw new RestBusinessException("Page must be greater than or equal to 0");
        }
    }

    private void validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new RestBusinessException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private Sort parseSort(
            String sort,
            String defaultSort,
            Sort.Direction defaultDirection,
            Map<String, String> allowedSorts
    ) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(defaultDirection, allowedSorts.get(defaultSort));
        }

        String[] parts = sort.split(",");
        if (parts.length > 2) {
            throw new RestBusinessException("Sort must use the format field,direction");
        }

        String requestedField = parts[0].trim();
        String property = allowedSorts.get(requestedField);
        if (property == null) {
            throw new RestBusinessException("Unsupported sort field: " + requestedField);
        }

        Sort.Direction direction = defaultDirection;
        if (parts.length == 2 && StringUtils.hasText(parts[1])) {
            direction = parseDirection(parts[1].trim());
        }

        return Sort.by(direction, property);
    }

    private Sort.Direction parseDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new RestBusinessException("Sort direction must be asc or desc");
        }
    }
}
