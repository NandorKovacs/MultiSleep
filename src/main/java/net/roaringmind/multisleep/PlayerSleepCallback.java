package net.roaringmind.multisleep;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerSleepCallback {
  Event<PlayerSleepCallback> EVENT = EventFactory.createArrayBacked(PlayerSleepCallback.class,
      (listeners) -> (player, bed) -> {
        for (PlayerSleepCallback listener : listeners) {
          ActionResult result = listener.interact(player, bed);

          if (result != ActionResult.PASS) {
            return result;
          }

        }
        return ActionResult.PASS;
      });

  ActionResult interact(PlayerEntity player, BedBlock bed);
}
