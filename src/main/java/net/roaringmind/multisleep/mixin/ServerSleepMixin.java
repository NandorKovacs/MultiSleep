package net.roaringmind.multisleep.mixin;

import static net.roaringmind.multisleep.MultiSleep.log;

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
  public void setTimeOfDay(long timeOfDay) {
    log(Level.INFO, "sleep didnt work");
  }

  @Shadow
  private void wakeSleepingPlayers() {
    log(Level.INFO, "wake didnt work");
  }

  @Shadow
  private void resetWeather() {
    log(Level.INFO, "weather didnt work");
  }

  @Inject(method = "tick", at = @At(value = "RETURN"))
  public void sleepInject(BooleanSupplier shouldKeepTicking, CallbackInfo cir) {

    if (!MultiSleep.shouldSleepNow || this.isDay()) {
      return;
    }

    log("[ServerSleepMixin] trying to sleep");

    boolean daycicle = this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE);
    boolean doweather = this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE);

    log("[ServerSleepMixin] daycicle: " + String.valueOf(daycicle));
    log("[ServerSleepMixin] weathercycle: " + String.valueOf(doweather));

    if (daycicle) {
      this.setTimeOfDay(0);
      log("[ServerSleepMixin] set time");
    }

    this.wakeSleepingPlayers();
    if (doweather) {
      this.resetWeather();
      log("[ServerSleepMixin] set weather");
    }

    log("[ServerSleepMixin] should sleep now false");
    MultiSleep.shouldSleepNow = false;
  }
}
