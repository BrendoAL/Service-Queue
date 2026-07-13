package com.brendo.queue.entity;

import com.brendo.queue.exception.InvalidTicketStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private Counter counter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Ticket(String number, TicketPriority priority) {
        this.number = number;
        this.priority = priority == null ? TicketPriority.NORMAL : priority;
    }

    public void call(Counter counter) {
        if (status != TicketStatus.WAITING) {
            throw new InvalidTicketStateException("Only waiting tickets can be called");
        }

        this.counter = counter;
        this.status = TicketStatus.CALLED;
        this.calledAt = LocalDateTime.now();
    }

    public void recall() {
        if (status != TicketStatus.CALLED && status != TicketStatus.IN_SERVICE) {
            throw new InvalidTicketStateException("Only called tickets can be recalled");
        }

        this.calledAt = LocalDateTime.now();
    }

    public void startService() {
        if (status != TicketStatus.CALLED) {
            throw new InvalidTicketStateException("Only called tickets can start service");
        }

        this.status = TicketStatus.IN_SERVICE;
    }

    public void transfer(Counter counter) {
        if (status != TicketStatus.CALLED && status != TicketStatus.IN_SERVICE) {
            throw new InvalidTicketStateException("Only called tickets can be transferred");
        }

        this.counter = counter;
    }

    public void cancel() {
        if (status == TicketStatus.COMPLETED || status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Only active tickets can be cancelled");
        }

        this.status = TicketStatus.CANCELLED;
    }

    public void complete() {
        if (status != TicketStatus.CALLED && status != TicketStatus.IN_SERVICE) {
            throw new InvalidTicketStateException("Only called tickets can be completed");
        }

        this.status = TicketStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}
