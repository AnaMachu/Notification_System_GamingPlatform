package com.notifications.model;

/** Payload de {@link EventType#CHALLENGE_COMPLETED}. */
public record ChallengeCompletedPayload(String challengeName) implements EventPayload {}
