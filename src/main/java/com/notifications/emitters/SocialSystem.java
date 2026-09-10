package com.notifications.emitters;

import com.notifications.bus.EventBus;
import com.notifications.bus.GameEvent;
import com.notifications.model.*;

/**
 * API pública que usarían las partes reales del juego para anunciar
 * eventos sociales. No conoce a los suscriptores y solo publica al bus.
 */
public class SocialSystem {
    private final EventBus bus;
    public SocialSystem(EventBus bus) { this.bus = bus; }

    public void friendRequestSent(int fromUserId, int toUserId) {
        bus.publish(new GameEvent(toUserId, EventType.FRIEND_REQUEST, new FriendRequestPayload(fromUserId)));
    }

    public void friendRequestAccepted(int fromUserId, int toUserId) {
        bus.publish(new GameEvent(toUserId, EventType.FRIEND_ACCEPTED, new FriendAcceptedPayload(fromUserId)));
    }

    public void newFollower(int followerId, int followedUserId) {
        bus.publish(new GameEvent(followedUserId, EventType.NEW_FOLLOWER, new NewFollowerPayload(followerId)));
    }
}
