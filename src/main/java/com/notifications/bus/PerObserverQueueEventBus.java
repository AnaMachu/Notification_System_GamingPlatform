package com.notifications.bus;

import com.notifications.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación de {@link EventBus} donde cada {@link EventListener} tiene
 * su propia {@link BlockingQueue} y su propio hilo consumidor dedicado 
 *
 * <p><b>Aislamiento real:</b> si un observer se queda atorado procesando un
 * evento, solo su propio hilo se bloquea. Los demás observers, con sus
 * propias colas y sus propios hilos, siguen procesando eventos con
 * normalidad — no se enteran del problema.
 *
 * <p><b>Backpressure acotado:</b> cada cola tiene capacidad fija
 * ({@link #QUEUE_CAPACITY}). Si la cola de un observer específico se llena
 * (porque ese observer no da abasto), los eventos nuevos para ESE observer
 * se descartan y se registra una advertencia — pero eso no afecta en nada
 * la entrega a los demás observers, cuyas colas están intactas.
 *
 * <p> Hay que llamar {@link #shutdown()}
 * antes de terminar el programa para detener todos los hilos de forma
 * ordenada.
 */
public class PerObserverQueueEventBus implements EventBus {
    private static final Logger log = LoggerFactory.getLogger(PerObserverQueueEventBus.class);

    private static final GameEvent POISON_PILL = new GameEvent(-1, null, null);
    private static final int QUEUE_CAPACITY = 100;

    // Qué tipos le interesan a cada observer — para saber a quién enrutar cada evento
    private final Map<EventType, List<EventListener>> subscribers = new ConcurrentHashMap<>();

    // La infraestructura dedicada de cada observer: su cola + su hilo consumidor
    private final Map<EventListener, ObserverConsumer> consumers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, EventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        // Si es la primera vez que este listener se suscribe a algo, se le crea
        // su cola y su hilo dedicados. Si ya tenía uno, se reutiliza
        consumers.computeIfAbsent(listener, this::startConsumerFor);
    }

    @Override
    public void unsubscribe(EventType type, EventListener listener) {
        List<EventListener> listeners = subscribers.get(type);
        if (listeners != null) {
            listeners.remove(listener);
        }

        boolean stillSubscribedToSomething = subscribers.values().stream()
            .anyMatch(list -> list.contains(listener));
        if (!stillSubscribedToSomething) {
            ObserverConsumer consumer = consumers.remove(listener);
            if (consumer != null) {
                consumer.shutdown();
            }
        }
    }

    @Override
    public void publish(GameEvent event) {
        for (EventListener listener : subscribers.getOrDefault(event.getType(), List.of())) {
            ObserverConsumer consumer = consumers.get(listener);
            if (consumer == null) {
                continue;
            }
            boolean accepted = consumer.queue().offer(event);
            if (!accepted) {
                log.warn("Cola llena para {} — evento {} descartado (no afecta a otros observers)",
                    listener.getClass().getSimpleName(), event.getType());
            }
        }
    }

    private ObserverConsumer startConsumerFor(EventListener listener) {
        BlockingQueue<GameEvent> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        String threadName = "observer-consumer-" + listener.getClass().getSimpleName();
        Thread thread = new Thread(() -> consumeLoop(listener, queue), threadName);
        thread.start();
        return new ObserverConsumer(queue, thread);
    }

    private void consumeLoop(EventListener listener, BlockingQueue<GameEvent> queue) {
        while (true) {
            try {
                GameEvent event = queue.take(); 
                if (event == POISON_PILL) {
                    break;
                }
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    // consumidor se registra el error y se sigue con el siguiente evento.
                    log.error("Error procesando evento en {}", listener.getClass().getSimpleName(), e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.debug("Hilo consumidor de {} detenido", listener.getClass().getSimpleName());
    }

    /**
     * Detiene TODOS los hilos consumidores (uno por cada observer activo),
     * esperando a que cada uno termine de procesar lo que ya tenía en su cola.
     */
    public void shutdown() {
        for (ObserverConsumer consumer : consumers.values()) {
            consumer.shutdown();
        }
    }

    private record ObserverConsumer(BlockingQueue<GameEvent> queue, Thread thread) {
        void shutdown() {
            queue.offer(POISON_PILL);
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
