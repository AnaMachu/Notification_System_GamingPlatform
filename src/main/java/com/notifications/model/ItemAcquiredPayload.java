package com.notifications.model;

/** Payload de {@link EventType#ITEM_ACQUIRED}. */
public record ItemAcquiredPayload(String itemName) implements EventPayload {}
