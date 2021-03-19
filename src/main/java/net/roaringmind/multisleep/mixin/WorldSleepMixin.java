package net.roaringmind.multisleep.mixin;

import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.roaringmind.multisleep.WorldSleepCallback;

@Mixin(ServerWorld.class)
public class WorldSleepMixin {
  ServerWorld self = (ServerWorld) (Object) this;
  
  @Shadow
  private boolean allPlayersSleeping;

  @Inject(method = "tick", at = @At("HEAD"))
  public void startSleep(final CallbackInfo info, BooleanSupplier shouldKeepTicking) {
    ActionResult result = WorldSleepCallback.EVENT.invoker().interact();
    if (result == ActionResult.SUCCESS) {
      this.allPlayersSleeping = true;
    }
  }
}
