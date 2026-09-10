package com.notifications.model;

/** Payload de {@link EventType#PVP_DEFEATED}. */
public record PvpDefeatedPayload(int attackerId) implements EventPayload {}
