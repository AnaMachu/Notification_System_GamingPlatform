package com.notifications;

import com.notifications.bus.EventListener;
import com.notifications.bus.GameEvent;
import com.notifications.bus.PerObserverQueueEventBus;
import com.notifications.model.EventType;
import com.notifications.model.LevelUpPayload;
import com.notifications.model.NewFollowerPayload;
import com.notifications.service.AnalyticsListener;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas del contrato de {@link com.notifications.bus.EventBus} contra
 * {@link PerObserverQueueEventBus} — la implementación asíncrona con cola
 * dedicada por observer, actualmente usada en {@link RandomEventSystem}.
 *
 * <p>Como esta implementación es asíncrona, las aserciones no pueden correr
 * inmediatamente después de {@code publish()}. En vez de usar
 * {@code Thread.sleep(...)} (frágil, adivina cuánto esperar), estas pruebas
 * aprovechan que tanto {@code shutdown()} como {@code unsubscribe()} hacen
 * {@code Thread.join()} internamente — es decir, esperan de verdad a que el
 * hilo consumidor termine antes de continuar. Eso da pruebas deterministas,
 * sin condiciones de carrera.
 */
class PerObserverQueueEventBusTest {

    @Test
    void busEntregaSoloAListenersSuscritosAEseTipo() {
        PerObserverQueueEventBus bus = new PerObserverQueueEventBus();
        EventListener levelUpListener = mock(EventListener.class);
        EventListener itemListener = mock(EventListener.class);

        bus.subscribe(EventType.LEVEL_UP, levelUpListener);
        bus.subscribe(EventType.ITEM_ACQUIRED, itemListener);

        GameEvent event = new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(10));
        bus.publish(event);

        bus.shutdown(); // espera a que AMBOS hilos consumidores terminen antes de verificar

        verify(levelUpListener, times(1)).onEvent(event);
        verify(itemListener, never()).onEvent(any());
    }

    @Test
    void unsubscribeDetieneLaEntregaDeEventosFuturos() {
        PerObserverQueueEventBus bus = new PerObserverQueueEventBus();
        EventListener listener = mock(EventListener.class);
        bus.subscribe(EventType.LEVEL_UP, listener);

        bus.publish(new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(1)));

        // unsubscribe() detiene y espera (join) a que el hilo termine de
        // procesar lo que ya tenía en su cola ANTES de retornar — por eso el
        // primer evento queda garantizado como procesado en este punto.
        bus.unsubscribe(EventType.LEVEL_UP, listener);

        // El listener ya no está en subscribers: este evento nunca se
        // encola para él.
        bus.publish(new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(2)));

        verify(listener, times(1)).onEvent(any()); // solo el primero, no el segundo
    }

    @Test
    void analyticsListenerCuentaCorrectamentePorTipo() {
        PerObserverQueueEventBus bus = new PerObserverQueueEventBus();
        AnalyticsListener analytics = new AnalyticsListener();
        analytics.registerTo(bus);

        bus.publish(new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(1)));
        bus.publish(new GameEvent(2, EventType.LEVEL_UP, new LevelUpPayload(2)));
        bus.publish(new GameEvent(1, EventType.NEW_FOLLOWER, new NewFollowerPayload(3)));

        bus.shutdown(); // espera a que el hilo consumidor de analytics procese los 3

        assertEquals(2, analytics.getCount(EventType.LEVEL_UP));
        assertEquals(1, analytics.getCount(EventType.NEW_FOLLOWER));
        assertEquals(0, analytics.getCount(EventType.ITEM_ACQUIRED));
    }

    @Test
    void analyticsListenerDejaDeContarTrasDesuscribirse() {
        PerObserverQueueEventBus bus = new PerObserverQueueEventBus();
        AnalyticsListener analytics = new AnalyticsListener();
        analytics.registerTo(bus);

        bus.publish(new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(1)));
        analytics.unregisterFrom(bus); // join() interno: el primer evento ya se contó cuando esto retorna
        bus.publish(new GameEvent(1, EventType.LEVEL_UP, new LevelUpPayload(2)));

        assertEquals(1, analytics.getCount(EventType.LEVEL_UP));
    }
}
