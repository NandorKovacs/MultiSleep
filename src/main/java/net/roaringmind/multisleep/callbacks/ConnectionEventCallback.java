package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface ConnectionEventCallback {
    Event<ConnectionEventCallback> EVENT = EventFactory.createArrayBacked(ConnectionEventCallback.class,
      (listeners) -> (isjoin, player) -> {
        for (ConnectionEventCallback listener : listeners) {
          listener.interact(isjoin, player);
        }
      });

  void interact(Boolean isjoin, PlayerEntity player);
}
