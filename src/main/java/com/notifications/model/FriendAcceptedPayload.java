package com.notifications.model;

/** Payload de {@link EventType#FRIEND_ACCEPTED}. */
public record FriendAcceptedPayload(int fromUserId) implements EventPayload {}
