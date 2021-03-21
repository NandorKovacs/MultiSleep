package net.roaringmind.multisleep.mixin;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.roaringmind.multisleep.callbacks.WorldSleepCallback;

@Mixin(ServerWorld.class)
public abstract class WorldSleepMixin extends World {

  WorldSleepMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, final DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
    super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
  }

  ServerWorld self = (ServerWorld) (Object) this;

  @Shadow
  private boolean allPlayersSleeping;

  @Inject(method = "tick", at = @At(value = "HEAD"))
  public void startSleep(BooleanSupplier shouldKeepTicking, final CallbackInfo info) {
    ActionResult result = WorldSleepCallback.EVENT.invoker().interact();
    if (result == ActionResult.SUCCESS) {
      this.allPlayersSleeping = true;
    }
  }
}
