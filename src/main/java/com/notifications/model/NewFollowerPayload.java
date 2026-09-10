package com.notifications.model;

/** Payload de {@link EventType#NEW_FOLLOWER}. */
public record NewFollowerPayload(int followerId) implements EventPayload {}
