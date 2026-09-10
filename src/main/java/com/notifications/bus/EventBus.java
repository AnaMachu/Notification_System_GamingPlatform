package com.notifications.bus;

import com.notifications.model.EventType;

/**
 * Contrato de publish/subscribe que desacopla a los emisores de eventos
 * de quienes reaccionan a ellos.
 */
public interface EventBus {
    /**
     * Registra un listener para un tipo de evento.
     *
     * @param type tipo de evento a escuchar
     * @param listener quién reacciona al evento
     */
    void subscribe(EventType type, EventListener listener);

    /**
     * Da de baja un listener previamente suscrito a un tipo de evento.
     *
     * @param type tipo de evento del que desuscribirse
     * @param listener listener a remover
     */
    void unsubscribe(EventType type, EventListener listener);

    /**
     * @param event el evento a publicar
     */
    void publish(GameEvent event);
}
