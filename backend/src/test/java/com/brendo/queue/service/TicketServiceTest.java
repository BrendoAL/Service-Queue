package com.brendo.queue.service;

import com.brendo.queue.dto.request.CallNextTicketRequest;
import com.brendo.queue.dto.request.CreateTicketRequest;
import com.brendo.queue.dto.response.QueueStatusResponse;
import com.brendo.queue.dto.response.TicketResponse;
import com.brendo.queue.entity.Counter;
import com.brendo.queue.entity.Ticket;
import com.brendo.queue.entity.TicketPriority;
import com.brendo.queue.entity.TicketSequence;
import com.brendo.queue.entity.TicketStatus;
import com.brendo.queue.exception.InvalidTicketStateException;
import com.brendo.queue.exception.ResourceNotFoundException;
import com.brendo.queue.repository.CounterRepository;
import com.brendo.queue.repository.TicketRepository;
import com.brendo.queue.repository.TicketSequenceRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final TicketSequenceRepository ticketSequenceRepository = mock(TicketSequenceRepository.class);
    private final CounterRepository counterRepository = mock(CounterRepository.class);
    private final TicketService ticketService = new TicketService(
        ticketRepository,
        ticketSequenceRepository,
        counterRepository
    );

    @Test
    void createGeneratesDailySequentialTicketNumber() {
        TicketSequence sequence = new TicketSequence(LocalDate.now());
        when(ticketSequenceRepository.findBySequenceDate(LocalDate.now())).thenReturn(Optional.of(sequence));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.create(new CreateTicketRequest(TicketPriority.PRIORITY));

        assertThat(response.number()).isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "0001");
        assertThat(response.priority()).isEqualTo(TicketPriority.PRIORITY);
        assertThat(response.status()).isEqualTo(TicketStatus.WAITING);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void callNextAssignsActiveCounterAndMarksTicketAsCalled() {
        Counter counter = new Counter("Guiche 1");
        Ticket ticket = new Ticket("0001", TicketPriority.NORMAL);
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));
        when(ticketRepository.findFirstByStatusOrderByPriorityDescCreatedAtAsc(TicketStatus.WAITING))
            .thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.callNext(new CallNextTicketRequest(1L));

        assertThat(response.status()).isEqualTo(TicketStatus.CALLED);
        assertThat(response.counterName()).isEqualTo("Guiche 1");
        assertThat(response.calledAt()).isNotNull();
    }

    @Test
    void callNextThrowsWhenCounterIsInactive() {
        Counter counter = new Counter("Guiche 1");
        counter.deactivate();
        when(counterRepository.findById(1L)).thenReturn(Optional.of(counter));

        assertThatThrownBy(() -> ticketService.callNext(new CallNextTicketRequest(1L)))
            .isInstanceOf(InvalidTicketStateException.class)
            .hasMessage("Counter is inactive: 1");
    }

    @Test
    void completeMarksCalledTicketAsCompleted() {
        Counter counter = new Counter("Guiche 1");
        Ticket ticket = new Ticket("0001", TicketPriority.NORMAL);
        ticket.call(counter);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.complete(1L);

        assertThat(response.status()).isEqualTo(TicketStatus.COMPLETED);
        assertThat(response.completedAt()).isNotNull();
    }

    @Test
    void completeThrowsWhenTicketWasNotCalled() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(new Ticket("0001", TicketPriority.NORMAL)));

        assertThatThrownBy(() -> ticketService.complete(1L))
            .isInstanceOf(InvalidTicketStateException.class)
            .hasMessage("Only called tickets can be completed");
    }

    @Test
    void queueStatusReturnsCountersAndLastCalledTickets() {
        Ticket calledTicket = new Ticket("0001", TicketPriority.NORMAL);
        calledTicket.call(new Counter("Guiche 1"));
        when(ticketRepository.countByStatus(TicketStatus.WAITING)).thenReturn(3L);
        when(ticketRepository.countByStatus(TicketStatus.CALLED)).thenReturn(1L);
        when(ticketRepository.countByStatus(TicketStatus.IN_SERVICE)).thenReturn(0L);
        when(ticketRepository.findTop5ByStatusOrderByCalledAtDesc(TicketStatus.CALLED))
            .thenReturn(List.of(calledTicket));

        QueueStatusResponse response = ticketService.queueStatus();

        assertThat(response.waiting()).isEqualTo(3);
        assertThat(response.called()).isEqualTo(1);
        assertThat(response.lastCalled()).hasSize(1);
    }

    @Test
    void getByIdThrowsWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Ticket not found: 99");
    }
}
