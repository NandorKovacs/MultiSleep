package net.roaringmind.multisleep.mixin;

import static net.roaringmind.multisleep.MultiSleep.log;

import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.roaringmind.multisleep.util.ServerSleepAccess;

@Mixin(ServerWorld.class)
public abstract class ServerSleepMixin extends World implements ServerSleepAccess {

  ServerSleepMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, final DimensionType dimensionType,
      Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
    super(properties, registryRef, (RegistryEntry<DimensionType>) dimensionType, profiler, isClient, debugWorld, seed);
  }

  @Shadow
  public void setTimeOfDay(long timeOfDay) {
    log(Level.INFO, "shadowing setTimeOfDay didnt work");
  }

  @Shadow
  private void wakeSleepingPlayers() {
    log(Level.INFO, "shadowing wakeSleepingPlayers didnt work");
  }

  @Shadow
  private void resetWeather() {
    log(Level.INFO, "shadowing resetWeather didnt work");
  }

  @Override
  public void sleep() {
    if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
      long l = this.properties.getTimeOfDay() + 24000L;
      this.setTimeOfDay(l - l % 24000L);
    }

    this.wakeSleepingPlayers();
    if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
      this.resetWeather();
    }
  }
}
