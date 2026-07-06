package br.com.appointments.flowpay.service.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.appointments.flowpay.exceptions.RestBusinessException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableFactoryTest {

    private final PageableFactory pageableFactory = new PageableFactory();

    @Test
    void shouldCreatePageableWithMappedSort() {
        Pageable pageable = pageableFactory.create(
                1,
                20,
                "team,desc",
                "name",
                Sort.Direction.ASC,
                Map.of("name", "name", "team", "team.name")
        );

        Sort.Order order = pageable.getSort().getOrderFor("team.name");

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void shouldRejectUnsupportedSortField() {
        assertThatThrownBy(() -> pageableFactory.create(
                0,
                10,
                "unsupported,asc",
                "name",
                Sort.Direction.ASC,
                Map.of("name", "name")
        )).isInstanceOf(RestBusinessException.class)
                .hasMessageContaining("Unsupported sort field");
    }

    @Test
    void shouldRejectSizeGreaterThanLimit() {
        assertThatThrownBy(() -> pageableFactory.create(
                0,
                101,
                null,
                "name",
                Sort.Direction.ASC,
                Map.of("name", "name")
        )).isInstanceOf(RestBusinessException.class)
                .hasMessageContaining("Size must be between");
    }
}
