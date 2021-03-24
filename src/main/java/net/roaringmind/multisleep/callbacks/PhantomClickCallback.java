package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface PhantomClickCallback {
  Event<PhantomClickCallback> EVENT = EventFactory.createArrayBacked(PhantomClickCallback.class,
      (listeners) -> (player) -> {
        for (PhantomClickCallback listener : listeners) {
          listener.interact(player);
        }
      });

  void interact(PlayerEntity player);
}
