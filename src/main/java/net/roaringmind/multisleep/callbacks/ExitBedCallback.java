package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface ExitBedCallback {
  Event<ExitBedCallback> EVENT = EventFactory.createArrayBacked(ExitBedCallback.class,
      (listeners) -> (player) -> {
        for (ExitBedCallback listener : listeners) {
          ActionResult result = listener.interact(player);
          if (result != ActionResult.PASS) {
            return result;
          }
        }
        return ActionResult.PASS;
      });

  ActionResult interact(PlayerEntity player);
}
