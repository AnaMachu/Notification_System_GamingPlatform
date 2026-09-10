package com.notifications.model;

/**
 * Todos los tipos de evento que el sistema sabe procesar. Cada valor
 * tiene un {@link EventPayload} asociado y una {@link NotificationCategory}.
 */
public enum EventType {
    LEVEL_UP, ITEM_ACQUIRED, CHALLENGE_COMPLETED, PVP_DEFEATED,
    FRIEND_REQUEST, FRIEND_ACCEPTED, NEW_FOLLOWER
}
