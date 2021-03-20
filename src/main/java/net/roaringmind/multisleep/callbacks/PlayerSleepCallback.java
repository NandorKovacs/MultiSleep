package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface PlayerSleepCallback {
  Event<PlayerSleepCallback> EVENT = EventFactory.createArrayBacked(PlayerSleepCallback.class,
      (listeners) -> (player, pos) -> {
        for (PlayerSleepCallback listener : listeners) {
          ActionResult result = listener.interact(player, pos);

          if (result != ActionResult.PASS) {
            return result;
          }

        }
        return ActionResult.PASS;
      });

  ActionResult interact(PlayerEntity player, BlockPos bedPosition);
}
