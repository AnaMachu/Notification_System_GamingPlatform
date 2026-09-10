package com.notifications.bus;

import com.notifications.model.EventPayload;
import com.notifications.model.EventType;

/**
 * Evento crudo que viaja por el {@link EventBus}
 */
public class GameEvent {
    private final int userId;
    private final EventType type;
    private final EventPayload payload;

    public GameEvent(int userId, EventType type, EventPayload payload) {
        this.userId = userId;
        this.type = type;
        this.payload = payload;
    }

    public int getUserId() { return userId; }
    public EventType getType() { return type; }
    public EventPayload getPayload() { return payload; }
}
