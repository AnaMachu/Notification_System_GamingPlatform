package com.notifications.service;

import com.notifications.model.NotificationCategory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guarda si cada usuario quiere recibir notificaciones de cada
 * {@link NotificationCategory}. Si no hay preferencia explícita, se
 * asume habilitada por defecto.
 */
public class UserPreferencesService {
    private final Map<Integer, Map<NotificationCategory, Boolean>> preferences = new ConcurrentHashMap<>();

    public void setPreference(int userId, NotificationCategory category, boolean enabled) {
        preferences.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(category, enabled);
    }

    public boolean isEnabled(int userId, NotificationCategory category) {
        return preferences.getOrDefault(userId, Collections.emptyMap())
                           .getOrDefault(category, true);
    }
}
