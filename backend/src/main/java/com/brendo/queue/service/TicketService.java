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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TicketService {

    private static final DateTimeFormatter TICKET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

    private final TicketRepository ticketRepository;
    private final TicketSequenceRepository ticketSequenceRepository;
    private final CounterRepository counterRepository;
    private final QueueEventPublisher queueEventPublisher;

    public TicketService(
            TicketRepository ticketRepository,
            TicketSequenceRepository ticketSequenceRepository,
            CounterRepository counterRepository,
            QueueEventPublisher queueEventPublisher) {
        this.ticketRepository = ticketRepository;
        this.ticketSequenceRepository = ticketSequenceRepository;
        this.counterRepository = counterRepository;
        this.queueEventPublisher = queueEventPublisher;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        TicketPriority priority = request == null || request.priority() == null
            ? TicketPriority.NORMAL
            : request.priority();
        Ticket ticket = new Ticket(nextTicketNumber(), priority);

        TicketResponse response = toResponse(ticketRepository.save(ticket));
        queueEventPublisher.publishTicketChanged("CREATED", response, this::queueStatus);
        return response;
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public TicketResponse callNext(CallNextTicketRequest request) {
        Counter counter = counterRepository.findById(request.counterId())
            .orElseThrow(() -> new ResourceNotFoundException("Counter not found: " + request.counterId()));

        if (!counter.isActive()) {
            throw new InvalidTicketStateException("Counter is inactive: " + request.counterId());
        }

        Ticket ticket = ticketRepository.findFirstByStatusOrderByPriorityDescCreatedAtAsc(TicketStatus.WAITING)
            .orElseThrow(() -> new ResourceNotFoundException("No waiting tickets available"));

        ticket.call(counter);
        TicketResponse response = toResponse(ticket);
        queueEventPublisher.publishTicketChanged("CALLED", response, this::queueStatus);
        return response;
    }

    @Transactional
    public TicketResponse recall(Long id) {
        Ticket ticket = findById(id);
        ticket.recall();
        TicketResponse response = toResponse(ticket);
        queueEventPublisher.publishTicketChanged("RECALLED", response, this::queueStatus);
        return response;
    }

    @Transactional
    public TicketResponse complete(Long id) {
        Ticket ticket = findById(id);
        ticket.complete();
        TicketResponse response = toResponse(ticket);
        queueEventPublisher.publishTicketChanged("COMPLETED", response, this::queueStatus);
        return response;
    }

    @Transactional(readOnly = true)
    public QueueStatusResponse queueStatus() {
        return new QueueStatusResponse(
            ticketRepository.countByStatus(TicketStatus.WAITING),
            ticketRepository.countByStatus(TicketStatus.CALLED),
            ticketRepository.countByStatus(TicketStatus.IN_SERVICE),
            ticketRepository.findTop5ByStatusOrderByCalledAtDesc(TicketStatus.CALLED).stream()
                .map(this::toResponse)
                .toList()
        );
    }

    private String nextTicketNumber() {
        LocalDate today = LocalDate.now();
        TicketSequence sequence = ticketSequenceRepository.findBySequenceDate(today)
            .orElseGet(() -> ticketSequenceRepository.save(new TicketSequence(today)));

        int nextNumber = sequence.nextNumber();
        return "%s%04d".formatted(today.format(TICKET_DATE_FORMAT), nextNumber);
    }

    private Ticket findById(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    private TicketResponse toResponse(Ticket ticket) {
        Counter counter = ticket.getCounter();

        return new TicketResponse(
            ticket.getId(),
            ticket.getNumber(),
            ticket.getStatus(),
            ticket.getPriority(),
            counter == null ? null : counter.getId(),
            counter == null ? null : counter.getName(),
            ticket.getCreatedAt(),
            ticket.getCalledAt(),
            ticket.getCompletedAt()
        );
    }
}
