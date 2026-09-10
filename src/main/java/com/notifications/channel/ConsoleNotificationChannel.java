package com.notifications.channel;

import com.notifications.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementación mock de {@link NotificationChannel}: solo registra en el log. */
public class ConsoleNotificationChannel implements NotificationChannel {
    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationChannel.class);

    @Override
    public void send(Notification notification) {
        log.info("Enviado -> {}", notification);
    }
}
