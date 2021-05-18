package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;
import net.roaringmind.multisleep.callbacks.TrySleepCallback;
import net.roaringmind.multisleep.gui.ClickTypes;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerCommands();

    registerEvents();
  }

  private static Key<IntRule> registerIntGamerule(String name, int min, int max, int startValue) {
    if (GameRuleRegistry.hasRegistration(name)) {
      log(Level.FATAL, "Can't register gamerule, gamerule with the id \"" + name
          + "\" is already existing. Resolve the issue, or there may be confilicts with other mods");
      return null;
    }
    return GameRuleRegistry.register(name, Category.MISC, GameRuleFactory.createIntRule(startValue, min, max));
  }
  public static Key<IntRule> minigameTimeframe = registerIntGamerule("minigameTimeframe-pickpocket", 0, 100, 100);

  //@formatter:off
  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("multisleep")
        .then(literal("forcesleep")
          .executes(ctx -> {
            sleep();
            return 0;
          })
        )
        .then(literal("sleep")
          .executes(ctx -> {
            vote(ctx.getSource().getPlayer(), true, true);
            return 0;
          })
        )
      );
    });
  }
  //@formatter:on

  private void registerEvents() {
    TrySleepCallback.EVENT.register((player, pos) -> {
      vote(player, true, true);
      return ActionResult.PASS;
    });
  }

  public static boolean shouldSleepNow = false;
  public static boolean isVoting = false;

  private static Set<UUID> sleepingPlayers = new HashSet<>();
  private static Set<UUID> awakePlayers = new HashSet<>();
  private static Set<UUID> permaSleepPlayers = new HashSet<>();

  public static Set<UUID> wantsPhantoms = new HashSet<>();

  private void sleep() {
    shouldSleepNow = true;
  }

  public static void vote(PlayerEntity player, boolean wantsSleep, boolean canStart) {
    if (!wantsSleep) {
      if (!isVoting) {
        return;
      }
      awakePlayers.add(player.getUuid());
    } else {
      if (!isVoting) {
        if (!canStart) {
          return;
        }

        startVoting(player);
      }
      sleepingPlayers.add(player.getUuid());
    }
    checkVotes(player.getServer());
  }

  private static void startVoting(PlayerEntity player) {
    for (PlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
      p.sendMessage(new LiteralText(player.getName() + " wants to sleep, please vote"), true);
      p.sendMessage(new LiteralText(player.getName() + " wants to sleep, please vote"), false);
    }

    // TODO: somehow start coundown

    isVoting = true;
  }

  private static void checkVotes(MinecraftServer server) {
    int requiredPercent = server.getGameRules().getInt(minigameTimeframe);
    int percentYes = sleepingPlayers.size() / server.getCurrentPlayerCount() * 100;
    int percentNo = 100 - percentYes;

    if (percentYes > requiredPercent) {
      startSleep();
    }

    if (percentNo > requiredPercent) {
      stopVoting();
    }
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

  public static void playerClick(PlayerEntity player, ClickTypes vote) {
    switch (vote) {
      case YES: {
        vote(player, true, false);
      }
      case NO: {
        vote(player, false, false);
      }
      case PERMA_SLEEP: {
        permaSleepPlayers.add(player.getUuid());
      }
    }
  }

  public static void setPhantomPreferences(UUID playerUUID, boolean on) {
    if (on) {
      wantsPhantoms.add(playerUUID);
    } else {
      wantsPhantoms.remove(playerUUID);
    }
  }
}
