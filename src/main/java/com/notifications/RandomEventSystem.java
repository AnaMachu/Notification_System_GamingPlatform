package com.notifications;

import com.notifications.bus.EventBus;
import com.notifications.bus.PerObserverQueueEventBus;
import com.notifications.channel.ConsoleNotificationChannel;
import com.notifications.channel.NotificationChannel;
import com.notifications.emitters.GameEngine;
import com.notifications.emitters.RandomEventGenerator;
import com.notifications.emitters.SocialSystem;
import com.notifications.model.NotificationCategory;
import com.notifications.service.AnalyticsListener;
import com.notifications.service.NotificationServiceImpl;
import com.notifications.service.UserPreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Arma el bus, sus suscriptores
 * y los emisores, y corre una serie de eventos aleatorios en tiempo simulado.
 */
public class RandomEventSystem {
    private static final Logger log = LoggerFactory.getLogger(RandomEventSystem.class);

    /**
     * Arma el sistema completo y corre una simulación de 15 eventos
     * aleatorios, desuscribiendo {@link AnalyticsListener} a la mitad.
     *
     * @param args no se usan
     * @throws InterruptedException si el hilo es interrumpido durante la pausa entre eventos
     */
    public static void main(String[] args) throws InterruptedException {
        PerObserverQueueEventBus bus = new PerObserverQueueEventBus();

        UserPreferencesService preferences = new UserPreferencesService();
        Random random = new Random();
        randomizePreferences(preferences, random);

        NotificationChannel channel = new ConsoleNotificationChannel();
        NotificationServiceImpl notificationService = new NotificationServiceImpl(preferences, channel);
        AnalyticsListener analytics = new AnalyticsListener();

        notificationService.registerTo(bus);
        analytics.registerTo(bus);

        GameEngine gameEngine = new GameEngine(bus);
        SocialSystem socialSystem = new SocialSystem(bus);
        RandomEventGenerator generator = new RandomEventGenerator(gameEngine, socialSystem);

        int totalEvents = 15;
        int analyticsCutoff = totalEvents / 2;
        log.info("--- Simulando {} eventos en tiempo real ---", totalEvents);

        for (int i = 0; i < totalEvents; i++) {
            if (i == analyticsCutoff) {
                analytics.unregisterFrom(bus);
                log.info("AnalyticsListener se desuscribió del bus (evento {})", i);
            }
            generator.generateRandomEvent();
            Thread.sleep(120 + random.nextInt(280));
        }
        bus.shutdown();
        analytics.printSummary();
    }

    private static void randomizePreferences(UserPreferencesService preferences, Random random) {
        for (int userId : new int[]{1, 2, 3, 4, 5}) {
            for (NotificationCategory category : NotificationCategory.values()) {
                boolean enabled = random.nextDouble() > 0.25;
                preferences.setPreference(userId, category, enabled);
            }
        }
    }
}
