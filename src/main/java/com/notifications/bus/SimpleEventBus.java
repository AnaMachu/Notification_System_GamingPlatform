package com.notifications.bus;

import com.notifications.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación en memoria y de un solo proceso de {@link EventBus}.
 *
 * <p>Thread-safe: {@link ConcurrentHashMap} para el mapa de suscriptores
 * y {@link CopyOnWriteArrayList} para cada lista de listeners, de modo
 * que subscribe/unsubscribe/publish pueden ocurrir desde distintos hilos
 * sin {@code ConcurrentModificationException}.
 */
public class SimpleEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(SimpleEventBus.class);

    private final Map<EventType, List<EventListener>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, EventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        log.debug("Suscrito nuevo listener a {}", type);
    }

    @Override
    public void unsubscribe(EventType type, EventListener listener) {
        List<EventListener> listeners = subscribers.get(type);
        if (listeners != null) {
            listeners.remove(listener);
            log.debug("Listener desuscrito de {}", type);
        }
    }

    @Override
    public void publish(GameEvent event) {
        for (EventListener listener : subscribers.getOrDefault(event.getType(), List.of())) {
            listener.onEvent(event);
        }
    }
}
