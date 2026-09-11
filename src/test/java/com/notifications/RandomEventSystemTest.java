package com.notifications;

import com.notifications.bus.GameEvent;
import com.notifications.channel.NotificationChannel;
import com.notifications.model.*;
import com.notifications.service.NotificationServiceImpl;
import com.notifications.service.UserPreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RandomEventSystemTest {

    private UserPreferencesService preferences;
    private NotificationChannel channel;
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        preferences = new UserPreferencesService();
        channel = mock(NotificationChannel.class);
        notificationService = new NotificationServiceImpl(preferences, channel);
    }

    @Test
    void preferenciaPorDefectoEsHabilitada() {
        assertTrue(preferences.isEnabled(99, NotificationCategory.GAME_EVENTS));
    }

    @Test
    void preferenciaExplicitaSeRespeta() {
        preferences.setPreference(1, NotificationCategory.SOCIAL_EVENTS, false);

        assertFalse(preferences.isEnabled(1, NotificationCategory.SOCIAL_EVENTS));
        assertTrue(preferences.isEnabled(1, NotificationCategory.GAME_EVENTS));
    }

    @Test
    void dispatchEnviaNotificacionConMensajeCorrecto() {
        GameEvent event = new GameEvent(5, EventType.LEVEL_UP, new LevelUpPayload(15));

        notificationService.onEvent(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(channel, times(1)).send(captor.capture());
        assertEquals("¡Felicidades! Alcanzaste el nivel 15!", captor.getValue().getMessage());
        assertEquals(5, captor.getValue().getRecipientId());
        assertEquals(NotificationCategory.GAME_EVENTS, captor.getValue().getCategory());
    }

    @Test
    void dispatchNoEnviaSiCategoriaDesactivada() {
        preferences.setPreference(4, NotificationCategory.SOCIAL_EVENTS, false);
        GameEvent event = new GameEvent(4, EventType.FRIEND_REQUEST, new FriendRequestPayload(2));

        notificationService.onEvent(event);

        verify(channel, never()).send(any());
    }

    @Test
    void todosLosTiposDePayloadGeneranMensajeSinExcepcion() {
        assertDoesNotThrow(() -> notificationService.onEvent(
            new GameEvent(1, EventType.ITEM_ACQUIRED, new ItemAcquiredPayload("Espada"))));
        assertDoesNotThrow(() -> notificationService.onEvent(
            new GameEvent(1, EventType.CHALLENGE_COMPLETED, new ChallengeCompletedPayload("Reto"))));
        assertDoesNotThrow(() -> notificationService.onEvent(
            new GameEvent(1, EventType.PVP_DEFEATED, new PvpDefeatedPayload(2))));
        assertDoesNotThrow(() -> notificationService.onEvent(
            new GameEvent(1, EventType.FRIEND_ACCEPTED, new FriendAcceptedPayload(2))));
        assertDoesNotThrow(() -> notificationService.onEvent(
            new GameEvent(1, EventType.NEW_FOLLOWER, new NewFollowerPayload(2))));
    }
}
