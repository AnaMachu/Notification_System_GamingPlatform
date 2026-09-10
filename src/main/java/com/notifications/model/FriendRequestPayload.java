package com.notifications.model;

/** Payload de {@link EventType#FRIEND_REQUEST}. */
public record FriendRequestPayload(int fromUserId) implements EventPayload {}
