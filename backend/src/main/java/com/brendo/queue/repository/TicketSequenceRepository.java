package com.brendo.queue.repository;

import com.brendo.queue.entity.TicketSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.Optional;

public interface TicketSequenceRepository extends JpaRepository<TicketSequence, LocalDate> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketSequence> findBySequenceDate(LocalDate sequenceDate);
}
