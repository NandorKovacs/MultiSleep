package net.roaringmind.multisleep.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface WorldSleepCallback {
  Event<WorldSleepCallback> EVENT = EventFactory.createArrayBacked(WorldSleepCallback.class,
      (listeners) -> () -> {
        for (WorldSleepCallback listener : listeners) {
          ActionResult result = listener.interact();

          if (result != ActionResult.PASS) {
            return result;
          }

        }
        return ActionResult.PASS;
      });

  ActionResult interact();

}
