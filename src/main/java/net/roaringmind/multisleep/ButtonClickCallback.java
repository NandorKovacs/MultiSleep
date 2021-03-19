package net.roaringmind.multisleep;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface ButtonClickCallback {
  Event<ButtonClickCallback> EVENT = EventFactory.createArrayBacked(ButtonClickCallback.class,
      (listeners) -> (player) -> {
        for (ButtonClickCallback listener : listeners) {
          ActionResult result = listener.interact(player);

          if (result != ActionResult.PASS) {
            return result;
          }

        }
        return ActionResult.PASS;
      });

  ActionResult interact(PlayerEntity player);
}
