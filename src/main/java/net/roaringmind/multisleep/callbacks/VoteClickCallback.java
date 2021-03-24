package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.roaringmind.multisleep.ClickTypes;

public interface VoteClickCallback {
  Event<VoteClickCallback> EVENT = EventFactory.createArrayBacked(VoteClickCallback.class,
      (listeners) -> (player, click) -> {
        for (VoteClickCallback listener : listeners) {
          listener.interact(player, click);
        }
      });

  void interact(PlayerEntity player, ClickTypes click);
}
