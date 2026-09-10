package com.notifications.model;

/**
 * Datos adicionales que viajan junto a un evento, específicos de cada
 * {@link EventType}. Al ser un {@code sealed interface}, el compilador
 * obliga a manejar cada implementación explícitamente en cualquier
 * {@code switch} 
 */
public sealed interface EventPayload
    permits LevelUpPayload, ItemAcquiredPayload, ChallengeCompletedPayload,
            PvpDefeatedPayload, FriendRequestPayload, FriendAcceptedPayload, NewFollowerPayload {
}
