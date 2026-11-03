package com.example.ecapi.notification;

import com.example.ecapi.dto.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final OrderNotificationService notificationService;

    @Async("orderNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event){
        notificationService.sendOrderConfirmation(event);
    }
}
