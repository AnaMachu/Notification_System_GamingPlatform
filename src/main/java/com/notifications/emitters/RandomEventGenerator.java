package com.notifications.emitters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.notifications.model.EventType;

import java.util.Random;

/**
 * Simula al juego, elige un tipo de
 * evento y un usuario al azar, arma un payload válido, y lo dispara
 * a través de la misma API pública ({@link GameEngine}/{@link SocialSystem})
 * que usaría cualquier parte real del juego.
 */
public class RandomEventGenerator {
    private static final Logger log = LoggerFactory.getLogger(RandomEventGenerator.class);

    private final GameEngine gameEngine;
    private final SocialSystem socialSystem;
    private final Random random;

    private static final int[] USER_POOL = {1, 2, 3, 4, 5};
    private static final String[] ITEMS =
        {"SwordOfAzeroth", "ShieldOfValor", "PhoenixFeather", "DragonScaleArmor", "RingOfHaste"};
    private static final String[] CHALLENGES =
        {"Derrotar al dragón", "Sobrevivir la noche", "Completar la mazmorra", "Vencer al jefe final"};

    public RandomEventGenerator(GameEngine gameEngine, SocialSystem socialSystem) {
        this.gameEngine = gameEngine;
        this.socialSystem = socialSystem;
        this.random = new Random();
    }

    public void generateRandomEvent() {
        EventType[] types = EventType.values();
        EventType type = types[random.nextInt(types.length)];
        int userId = randomUser();
        log.debug("Generando evento aleatorio: {} para usuario {}", type, userId);

        switch (type) {
            case LEVEL_UP -> gameEngine.playerLeveledUp(userId, 1 + random.nextInt(99));
            case ITEM_ACQUIRED -> gameEngine.itemAcquired(userId, randomFrom(ITEMS));
            case CHALLENGE_COMPLETED -> gameEngine.challengeCompleted(userId, randomFrom(CHALLENGES));
            case PVP_DEFEATED -> gameEngine.playerDefeated(userId, randomOtherUser(userId));
            case FRIEND_REQUEST -> socialSystem.friendRequestSent(randomOtherUser(userId), userId);
            case FRIEND_ACCEPTED -> socialSystem.friendRequestAccepted(randomOtherUser(userId), userId);
            case NEW_FOLLOWER -> socialSystem.newFollower(randomOtherUser(userId), userId);
        }
    }

    private int randomUser() {
        return USER_POOL[random.nextInt(USER_POOL.length)];
    }

    private int randomOtherUser(int excluding) {
        int candidate;
        do {
            candidate = randomUser();
        } while (candidate == excluding);
        return candidate;
    }

    private String randomFrom(String[] pool) {
        return pool[random.nextInt(pool.length)];
    }
}
