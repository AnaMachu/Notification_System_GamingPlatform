package com.notifications.model;

import java.time.Instant;

/**
 * Notificación ya construida y lista para ser entregada al cliente.
 * Es un objeto de datos inmutable: una vez creada, no cambia.
 */
public class Notification {
    private final int recipientId;
    private final NotificationCategory category;
    private final EventType type;
    private final String message;
    private final Instant timestamp;

    public Notification(int recipientId, NotificationCategory category,
                         EventType type, String message) {
        this.recipientId = recipientId;
        this.category = category;
        this.type = type;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public int getRecipientId() { return recipientId; }
    public NotificationCategory getCategory() { return category; }
    public EventType getType() { return type; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("[%s] Usuario %d -> %s", category, recipientId, message);
    }
}
