package com.brendo.queue.service;

import com.brendo.queue.dto.response.CounterReportResponse;
import com.brendo.queue.dto.response.DailyTicketReportResponse;
import com.brendo.queue.dto.response.ReportSummaryResponse;
import com.brendo.queue.entity.Counter;
import com.brendo.queue.entity.Ticket;
import com.brendo.queue.entity.TicketStatus;
import com.brendo.queue.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TicketRepository ticketRepository;

    public ReportService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDate from, LocalDate to) {
        DateRange range = normalizeRange(from, to);
        List<Ticket> tickets = findTickets(range);

        return new ReportSummaryResponse(
            range.from(),
            range.to(),
            tickets.size(),
            countByStatus(tickets, TicketStatus.WAITING),
            countByStatus(tickets, TicketStatus.CALLED),
            countByStatus(tickets, TicketStatus.IN_SERVICE),
            countByStatus(tickets, TicketStatus.COMPLETED),
            countByStatus(tickets, TicketStatus.CANCELLED),
            averageSeconds(tickets.stream()
                .filter(ticket -> ticket.getCalledAt() != null)
                .map(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getCalledAt()))
                .toList()),
            averageSeconds(tickets.stream()
                .filter(ticket -> ticket.getCalledAt() != null && ticket.getCompletedAt() != null)
                .map(ticket -> Duration.between(ticket.getCalledAt(), ticket.getCompletedAt()))
                .toList())
        );
    }

    @Transactional(readOnly = true)
    public List<DailyTicketReportResponse> daily(LocalDate from, LocalDate to) {
        DateRange range = normalizeRange(from, to);
        return findTickets(range).stream()
            .collect(Collectors.groupingBy(ticket -> ticket.getCreatedAt().toLocalDate()))
            .entrySet()
            .stream()
            .map(entry -> new DailyTicketReportResponse(
                entry.getKey(),
                entry.getValue().size(),
                countByStatus(entry.getValue(), TicketStatus.COMPLETED),
                countByStatus(entry.getValue(), TicketStatus.CANCELLED)
            ))
            .sorted(Comparator.comparing(DailyTicketReportResponse::date))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CounterReportResponse> counters(LocalDate from, LocalDate to) {
        DateRange range = normalizeRange(from, to);
        Map<CounterKey, List<Ticket>> ticketsByCounter = findTickets(range).stream()
            .filter(ticket -> ticket.getCounter() != null)
            .collect(Collectors.groupingBy(ticket -> CounterKey.from(ticket.getCounter())));

        return ticketsByCounter.entrySet()
            .stream()
            .map(entry -> new CounterReportResponse(
                entry.getKey().id(),
                entry.getKey().name(),
                entry.getValue().size(),
                countByStatus(entry.getValue(), TicketStatus.COMPLETED),
                averageSeconds(entry.getValue().stream()
                    .filter(ticket -> ticket.getCalledAt() != null && ticket.getCompletedAt() != null)
                    .map(ticket -> Duration.between(ticket.getCalledAt(), ticket.getCompletedAt()))
                    .toList())
            ))
            .sorted(Comparator.comparing(CounterReportResponse::counterName))
            .toList();
    }

    private List<Ticket> findTickets(DateRange range) {
        return ticketRepository.findAllByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAsc(
            range.from().atStartOfDay(),
            range.to().plusDays(1).atStartOfDay()
        );
    }

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate normalizedFrom = from == null ? LocalDate.now() : from;
        LocalDate normalizedTo = to == null ? normalizedFrom : to;

        if (normalizedTo.isBefore(normalizedFrom)) {
            throw new IllegalArgumentException("Report end date cannot be before start date");
        }

        return new DateRange(normalizedFrom, normalizedTo);
    }

    private long countByStatus(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream()
            .filter(ticket -> ticket.getStatus() == status)
            .count();
    }

    private long averageSeconds(List<Duration> durations) {
        return Math.round(durations.stream()
            .mapToLong(Duration::toSeconds)
            .average()
            .orElse(0));
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }

    private record CounterKey(Long id, String name) {

        private static CounterKey from(Counter counter) {
            return new CounterKey(counter.getId(), counter.getName());
        }
    }
}
