package com.notifications.bus;

/**
 * Cualquier componente que quiera reaccionar a eventos del bus
 * implementa esta interfaz y se suscribe con {@link EventBus#subscribe}.
 */
public interface EventListener {
    void onEvent(GameEvent event);
}
