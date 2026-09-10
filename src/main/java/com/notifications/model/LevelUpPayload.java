package com.notifications.model;

/** Payload de {@link EventType#LEVEL_UP}. */
public record LevelUpPayload(int newLevel) implements EventPayload {}
