package com.brendo.queue.service;

import com.brendo.queue.dto.response.CounterReportResponse;
import com.brendo.queue.dto.response.DailyTicketReportResponse;
import com.brendo.queue.dto.response.ReportSummaryResponse;
import com.brendo.queue.entity.Counter;
import com.brendo.queue.entity.Ticket;
import com.brendo.queue.entity.TicketPriority;
import com.brendo.queue.entity.TicketStatus;
import com.brendo.queue.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final ReportService reportService = new ReportService(ticketRepository);

    @Test
    void summaryAggregatesTicketCountsAndAverageTimes() {
        LocalDate reportDate = LocalDate.of(2026, 7, 12);
        Ticket waiting = ticket("0001", reportDate.atTime(9, 0));
        Ticket completed = completedTicket(reportDate);
        Ticket cancelled = ticket("0003", reportDate.atTime(11, 0));
        cancelled.cancel();
        when(ticketRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            reportDate.atStartOfDay(),
            reportDate.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(waiting, completed, cancelled));

        ReportSummaryResponse response = reportService.summary(reportDate, reportDate);

        assertThat(response.totalTickets()).isEqualTo(3);
        assertThat(response.waitingTickets()).isEqualTo(1);
        assertThat(response.completedTickets()).isEqualTo(1);
        assertThat(response.cancelledTickets()).isEqualTo(1);
        assertThat(response.averageWaitSeconds()).isEqualTo(300);
        assertThat(response.averageServiceSeconds()).isEqualTo(900);
    }

    @Test
    void dailyGroupsTicketsByCreationDate() {
        LocalDate firstDate = LocalDate.of(2026, 7, 12);
        LocalDate secondDate = LocalDate.of(2026, 7, 13);
        Ticket firstTicket = completedTicket(firstDate);
        Ticket secondTicket = ticket("0002", secondDate.atTime(9, 0));
        secondTicket.cancel();
        when(ticketRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            firstDate.atStartOfDay(),
            secondDate.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(firstTicket, secondTicket));

        List<DailyTicketReportResponse> response = reportService.daily(firstDate, secondDate);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).date()).isEqualTo(firstDate);
        assertThat(response.get(0).completedTickets()).isEqualTo(1);
        assertThat(response.get(1).date()).isEqualTo(secondDate);
        assertThat(response.get(1).cancelledTickets()).isEqualTo(1);
    }

    @Test
    void countersGroupsTicketsByAssignedCounter() {
        LocalDate reportDate = LocalDate.of(2026, 7, 12);
        Ticket completed = completedTicket(reportDate);
        when(ticketRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            reportDate.atStartOfDay(),
            reportDate.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(completed));

        List<CounterReportResponse> response = reportService.counters(reportDate, reportDate);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().counterId()).isEqualTo(1L);
        assertThat(response.getFirst().counterName()).isEqualTo("Guiche 1");
        assertThat(response.getFirst().calledTickets()).isEqualTo(1);
        assertThat(response.getFirst().completedTickets()).isEqualTo(1);
        assertThat(response.getFirst().averageServiceSeconds()).isEqualTo(900);
    }

    @Test
    void summaryRejectsInvalidDateRange() {
        assertThatThrownBy(() -> reportService.summary(
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 12)
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Report end date cannot be before start date");
    }

    private Ticket ticket(String number, LocalDateTime createdAt) {
        Ticket ticket = new Ticket(number, TicketPriority.NORMAL);
        ReflectionTestUtils.setField(ticket, "createdAt", createdAt);
        return ticket;
    }

    private Ticket completedTicket(LocalDate date) {
        Counter counter = new Counter("Guiche 1");
        ReflectionTestUtils.setField(counter, "id", 1L);
        Ticket ticket = ticket("0002", date.atTime(10, 0));
        ticket.call(counter);
        ticket.complete();
        ReflectionTestUtils.setField(ticket, "calledAt", date.atTime(10, 5));
        ReflectionTestUtils.setField(ticket, "completedAt", date.atTime(10, 20));
        return ticket;
    }
}
