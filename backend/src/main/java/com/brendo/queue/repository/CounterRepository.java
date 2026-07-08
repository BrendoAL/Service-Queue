package com.brendo.queue.repository;

import com.brendo.queue.entity.Counter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounterRepository extends JpaRepository<Counter, Long> {

    List<Counter> findAllByActive(boolean active);
}
