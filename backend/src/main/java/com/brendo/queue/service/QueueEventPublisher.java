package com.brendo.queue.service;

import com.brendo.queue.dto.response.QueueStatusResponse;
import com.brendo.queue.dto.response.TicketEventResponse;
import com.brendo.queue.dto.response.TicketResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

@Component
public class QueueEventPublisher {

    static final String TICKETS_TOPIC = "/topic/tickets";
    static final String QUEUE_STATUS_TOPIC = "/topic/queue/status";

    private final SimpMessagingTemplate messagingTemplate;

    public QueueEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishTicketChanged(
            String event,
            TicketResponse ticket,
            Supplier<QueueStatusResponse> queueStatusSupplier) {
        Runnable publish = () -> {
            messagingTemplate.convertAndSend(TICKETS_TOPIC, new TicketEventResponse(event, ticket));
            messagingTemplate.convertAndSend(QUEUE_STATUS_TOPIC, queueStatusSupplier.get());
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
            return;
        }

        publish.run();
    }
}
