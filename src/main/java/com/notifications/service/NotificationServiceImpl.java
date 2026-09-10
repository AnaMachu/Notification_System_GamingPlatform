package com.notifications.service;

import com.notifications.bus.EventBus;
import com.notifications.bus.EventListener;
import com.notifications.bus.GameEvent;
import com.notifications.channel.NotificationChannel;
import com.notifications.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Decide si un usuario debe recibir una
 * notificación (según {@link UserPreferencesService}), arma el mensaje
 * de texto correspondiente al {@link EventPayload}, y lo entrega a
 * través de un {@link NotificationChannel}.
 */
public class NotificationServiceImpl implements EventListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final UserPreferencesService preferencesService;
    private final NotificationChannel channel;

    private static final Map<EventType, NotificationCategory> CATEGORY_MAP = Map.of(
        EventType.LEVEL_UP,             NotificationCategory.GAME_EVENTS,
        EventType.ITEM_ACQUIRED,        NotificationCategory.GAME_EVENTS,
        EventType.CHALLENGE_COMPLETED,  NotificationCategory.GAME_EVENTS,
        EventType.PVP_DEFEATED,         NotificationCategory.GAME_EVENTS,
        EventType.FRIEND_REQUEST,       NotificationCategory.SOCIAL_EVENTS,
        EventType.FRIEND_ACCEPTED,      NotificationCategory.SOCIAL_EVENTS,
        EventType.NEW_FOLLOWER,         NotificationCategory.SOCIAL_EVENTS
    );

    public NotificationServiceImpl(UserPreferencesService preferencesService,
                                    NotificationChannel channel) {
        this.preferencesService = preferencesService;
        this.channel = channel;
    }

    
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
        NotificationCategory category = CATEGORY_MAP.get(event.getType());

        if (!preferencesService.isEnabled(event.getUserId(), category)) {
            log.info("Omitida (preferencia desactivada): {} para usuario {}", category, event.getUserId());
            return;
        }

        try {
            String message = buildMessage(event.getPayload());
            Notification notification = new Notification(event.getUserId(), category, event.getType(), message);
            channel.send(notification);
        } catch (Exception e) {
            log.error("Error enviando notificación para usuario {} (tipo {})", event.getUserId(), event.getType(), e);
        }
    }

    /**
     * Arma el texto final de la notificación según el tipo de payload.
     * Switch exhaustivo sobre {@link EventPayload}: si se agrega un
     * payload nuevo sin su {@code case} aquí, el código no compila.
     */
    private String buildMessage(EventPayload payload) {
        return switch (payload) {
            case LevelUpPayload p -> String.format("¡Felicidades! Alcanzaste el nivel %d!", p.newLevel());
            case ItemAcquiredPayload p -> String.format("¡Has obtenido: %s!", p.itemName());
            case ChallengeCompletedPayload p -> String.format("¡Completaste el desafío: %s!", p.challengeName());
            case PvpDefeatedPayload p -> String.format("Fuiste derrotado por el jugador %d.", p.attackerId());
            case FriendRequestPayload p -> String.format("El jugador '%d' te ha enviado una solicitud de amistad.", p.fromUserId());
            case FriendAcceptedPayload p -> String.format("El jugador '%d' aceptó tu solicitud de amistad.", p.fromUserId());
            case NewFollowerPayload p -> String.format("El jugador '%d' comenzó a seguirte.", p.followerId());
        };
    }
}
