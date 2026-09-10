package com.notifications.service;

import com.notifications.bus.EventBus;
import com.notifications.bus.EventListener;
import com.notifications.bus.GameEvent;
import com.notifications.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Suscriptor de demostración, totalmente independiente de
 * {@link NotificationServiceImpl} cuenta cuántos eventos de cada
 * tipo han pasado por el bus.
 */
public class AnalyticsListener implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsListener.class);

    private final Map<EventType, Integer> counts = new ConcurrentHashMap<>();

    public void registerTo(EventBus bus) {
        for (EventType type : EventType.values()) {
            bus.subscribe(type, this);
        }
    }

    public void unregisterFrom(EventBus bus) {
        for (EventType type : EventType.values()) {
            bus.unsubscribe(type, this);
        }
    }

    @Override
    public void onEvent(GameEvent event) {
        counts.merge(event.getType(), 1, Integer::sum);
    }

    public int getCount(EventType type) {
        return counts.getOrDefault(type, 0);
    }

    public void printSummary() {
        log.info("Resumen de analítica:");
        counts.forEach((type, count) -> log.info("   {}: {}", type, count));
    }
}
