package com.brendo.queue.service;

import com.brendo.queue.dto.request.CounterRequest;
import com.brendo.queue.dto.response.CounterResponse;
import com.brendo.queue.entity.Counter;
import com.brendo.queue.exception.ResourceNotFoundException;
import com.brendo.queue.repository.CounterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CounterService {

    private final CounterRepository counterRepository;

    public CounterService(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Transactional(readOnly = true)
    public List<CounterResponse> list(Boolean active) {
        List<Counter> counters = active == null
            ? counterRepository.findAll()
            : counterRepository.findAllByActive(active);

        return counters.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CounterResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public CounterResponse create(CounterRequest request) {
        Counter counter = new Counter(request.name().trim());
        return toResponse(counterRepository.save(counter));
    }

    @Transactional
    public CounterResponse update(Long id, CounterRequest request) {
        Counter counter = findById(id);
        counter.rename(request.name().trim());
        return toResponse(counter);
    }

    @Transactional
    public CounterResponse activate(Long id) {
        Counter counter = findById(id);
        counter.activate();
        return toResponse(counter);
    }

    @Transactional
    public CounterResponse deactivate(Long id) {
        Counter counter = findById(id);
        counter.deactivate();
        return toResponse(counter);
    }

    private Counter findById(Long id) {
        return counterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Counter not found: " + id));
    }

    private CounterResponse toResponse(Counter counter) {
        return new CounterResponse(
            counter.getId(),
            counter.getName(),
            counter.isActive(),
            counter.getCreatedAt()
        );
    }
}
