package com.brendo.queue.service;

import com.brendo.queue.dto.response.QueueStatusResponse;
import com.brendo.queue.dto.response.TicketEventResponse;
import com.brendo.queue.dto.response.TicketResponse;
import com.brendo.queue.entity.TicketPriority;
import com.brendo.queue.entity.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QueueEventPublisherTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
    private final QueueEventPublisher publisher = new QueueEventPublisher(messagingTemplate);

    @Test
    void publishTicketChangedSendsTicketEventAndQueueStatus() {
        TicketResponse ticket = new TicketResponse(
            1L,
            "2607120001",
            TicketStatus.CALLED,
            TicketPriority.NORMAL,
            10L,
            "Guiche 1",
            null,
            null,
            null
        );
        QueueStatusResponse status = new QueueStatusResponse(2, 1, 0, List.of(ticket));

        publisher.publishTicketChanged("CALLED", ticket, () -> status);

        verify(messagingTemplate)
            .convertAndSend(eq(QueueEventPublisher.TICKETS_TOPIC), eq(new TicketEventResponse("CALLED", ticket)));
        verify(messagingTemplate)
            .convertAndSend(eq(QueueEventPublisher.QUEUE_STATUS_TOPIC), eq(status));
    }
}
