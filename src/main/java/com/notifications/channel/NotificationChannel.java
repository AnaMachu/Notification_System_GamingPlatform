package com.notifications.channel;

import com.notifications.model.Notification;

/**
 * Punto de extensión para el canal de entrega final de una notificación
 * (in-app, push, email, WebSocket, etc.). Implementar esta interfaz es
 * todo lo que se necesita para agregar un canal nuevo 
 */
public interface NotificationChannel {
    void send(Notification notification);
}
