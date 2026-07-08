package com.brendo.queue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "ticket_sequence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketSequence {

    @Id
    @Column(name = "sequence_date", nullable = false)
    private LocalDate sequenceDate;

    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    public TicketSequence(LocalDate sequenceDate) {
        this.sequenceDate = sequenceDate;
    }

    public int nextNumber() {
        lastNumber++;
        return lastNumber;
    }
}
