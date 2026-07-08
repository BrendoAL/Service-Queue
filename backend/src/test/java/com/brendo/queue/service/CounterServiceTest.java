package com.brendo.queue.service;

import com.brendo.queue.dto.request.CounterRequest;
import com.brendo.queue.dto.response.CounterResponse;
import com.brendo.queue.entity.Counter;
import com.brendo.queue.exception.ResourceNotFoundException;
import com.brendo.queue.repository.CounterRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CounterServiceTest {

    private final CounterRepository counterRepository = mock(CounterRepository.class);
    private final CounterService counterService = new CounterService(counterRepository);

    @Test
    void listReturnsAllCountersWhenActiveFilterIsNotProvided() {
        when(counterRepository.findAll()).thenReturn(List.of(new Counter("Guiche 1")));

        List<CounterResponse> response = counterService.list(null);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().name()).isEqualTo("Guiche 1");
        assertThat(response.getFirst().active()).isTrue();
    }

    @Test
    void listFiltersByActiveStatus() {
        Counter inactiveCounter = new Counter("Guiche 2");
        inactiveCounter.deactivate();
        when(counterRepository.findAllByActive(false)).thenReturn(List.of(inactiveCounter));

        List<CounterResponse> response = counterService.list(false);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().active()).isFalse();
        verify(counterRepository).findAllByActive(false);
    }

    @Test
    void createTrimsNameAndPersistsCounter() {
        Counter savedCounter = new Counter("Guiche 1");
        when(counterRepository.save(org.mockito.ArgumentMatchers.any(Counter.class))).thenReturn(savedCounter);

        CounterResponse response = counterService.create(new CounterRequest(" Guiche 1 "));

        assertThat(response.name()).isEqualTo("Guiche 1");
        verify(counterRepository).save(org.mockito.ArgumentMatchers.any(Counter.class));
    }

    @Test
    void updateRenamesExistingCounter() {
        Counter counter = new Counter("Guiche antigo");
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        CounterResponse response = counterService.update(1L, new CounterRequest("Guiche novo"));

        assertThat(response.name()).isEqualTo("Guiche novo");
    }

    @Test
    void deactivateMarksCounterAsInactive() {
        Counter counter = new Counter("Guiche 1");
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        CounterResponse response = counterService.deactivate(1L);

        assertThat(response.active()).isFalse();
    }

    @Test
    void throwsWhenCounterDoesNotExist() {
        when(counterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> counterService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Counter not found: 99");
    }
}
