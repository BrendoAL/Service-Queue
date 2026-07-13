package com.brendo.queue.repository;

import com.brendo.queue.entity.Ticket;
import com.brendo.queue.entity.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ticket> findFirstByStatusOrderByPriorityDescCreatedAtAsc(TicketStatus status);

    long countByStatus(TicketStatus status);

    List<Ticket> findTop5ByStatusOrderByCalledAtDesc(TicketStatus status);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findAllByStatusOrderByPriorityDescCreatedAtAsc(TicketStatus status);
}
