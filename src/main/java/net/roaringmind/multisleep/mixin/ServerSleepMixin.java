package net.roaringmind.multisleep.mixin;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.roaringmind.multisleep.MultiSleep;

@Mixin(ServerWorld.class)
public abstract class ServerSleepMixin extends World {

  ServerSleepMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, final DimensionType dimensionType,
      Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
    super(properties, registryRef, dimensionType, profiler, isClient, debugWorld, seed);
  }

  @Shadow
  private boolean allPlayersSleeping;

  @Shadow
  public abstract void setTimeOfDay(long timeOfDay);

  @Shadow
  public abstract void wakeSleepingPlayers();

  @Shadow
  public abstract void resetWeather();

  @Inject(method = "tick", at = @At(value = "RETURN", args = { "log = true" }))
  public void sleepInject(BooleanSupplier shouldKeepTicking, CallbackInfo cir) {
    if (!MultiSleep.shouldSleepNow || this.isDay()) {
      return;
    }

    MultiSleep.log(Level.INFO, "will sleep now");

    if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
      long l = this.properties.getTimeOfDay() + 24000L;
      this.setTimeOfDay(l - l % 24000L);
    }

    this.wakeSleepingPlayers();
    if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
      this.resetWeather();
    }
    MultiSleep.shouldSleepNow = false;
  }
}
