package com.example.ecapi.notification;

import com.example.ecapi.dto.order.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderNotificationService {
    public void sendOrderConfirmation(OrderCreatedEvent event){
        log.info("Sending order confirmation to {} for order {}",
                event.email(),
                event.orderId());
    }

    // TODO: implement actual email sending
}
