package com.notifications.emitters;

import com.notifications.bus.EventBus;
import com.notifications.bus.GameEvent;
import com.notifications.model.*;

/**
 * API pública que usarían las partes reales del juego para anunciar
 * eventos de juego. No conoce a los suscriptores y solo publica al bus.
 */
public class GameEngine {
    private final EventBus bus;
    public GameEngine(EventBus bus) { this.bus = bus; }

    public void playerLeveledUp(int userId, int newLevel) {
        bus.publish(new GameEvent(userId, EventType.LEVEL_UP, new LevelUpPayload(newLevel)));
    }

    public void itemAcquired(int userId, String itemName) {
        bus.publish(new GameEvent(userId, EventType.ITEM_ACQUIRED, new ItemAcquiredPayload(itemName)));
    }

    public void challengeCompleted(int userId, String challengeName) {
        bus.publish(new GameEvent(userId, EventType.CHALLENGE_COMPLETED, new ChallengeCompletedPayload(challengeName)));
    }

    public void playerDefeated(int userId, int attackerId) {
        bus.publish(new GameEvent(userId, EventType.PVP_DEFEATED, new PvpDefeatedPayload(attackerId)));
    }
}
