package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;
import net.minecraft.world.World;
import net.roaringmind.multisleep.callbacks.TrySleepCallback;
import net.roaringmind.multisleep.countdown.Countdown;
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

  public static Key<IntRule> multiSleepPercent = registerIntGamerule("multiSleepPercent", 0, 100, 100);

  //@formatter:off
  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("multisleep")
        .then(literal("forcesleep")
          .executes(ctx -> {
            sleep(ctx.getSource().getMinecraftServer());
            return 0;
          })
        )
        .then(literal("sleep")
          .executes(ctx -> {
            vote(ctx.getSource().getPlayer(), true, false);
            return 0;
          })
        )

      );
      dispatcher.register(literal("opme")
        .executes(ctx -> {
          ctx.getSource().getMinecraftServer().getPlayerManager().addToOperators(ctx.getSource().getPlayer().getGameProfile());
          return 0;
        })
      );
    });
  }
  //@formatter:on

  private void registerEvents() {
    TrySleepCallback.EVENT.register((player, pos) -> {
      vote(player, true, true);
      return ActionResult.PASS;
    });
    ServerTickEvents.START_WORLD_TICK.register(world -> {
      if (currentCountdown.tick() < 0 && isVoting || shouldCancelVoting(world.getServer())) {
        cancelVoting();
      }
      if (isVoting && !initiator.isSleeping()) {
        cancelVoting();
      }
    });
  }

  public static boolean shouldSleepNow = false;
  public static boolean isVoting = false;

  private static Set<UUID> sleepingPlayers = new HashSet<>();
  private static Set<UUID> awakePlayers = new HashSet<>();
  private static PlayerEntity initiator = null;
  public static Set<UUID> permaSleepPlayers = new HashSet<>();
  private static Countdown currentCountdown = new Countdown(30 * 20);

  public static Set<UUID> wantsPhantoms = new HashSet<>();

  private static void sleep(MinecraftServer server) {
    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
      if (wantsPhantoms.contains(p.getUuid())) {
        continue;
      }
      p.getStatHandler().setStat(p, Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST), 0);
    }

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
      p.sendMessage(new LiteralText(player.getName().asString() + " wants to sleep, please vote"), true);
      p.sendMessage(new LiteralText(player.getName().asString() + " wants to sleep, please vote"), false);
    }

    currentCountdown.restart();

    initiator = player;
    isVoting = true;
  }

  private static boolean checkVotes(MinecraftServer server) {
    float requiredPercent = server.getGameRules().getInt(multiSleepPercent);
    float percentNo = (((float)sleepingPlayers.size() + (float)permaSleepPlayers.size()) / (float)server.getCurrentPlayerCount()) * (float)100;
    float percentYes = 100 - percentNo;

    System.out.println(percentYes + "----" + percentNo + "----" + requiredPercent + "----" + server.getCurrentPlayerCount());
    System.out.println("sleeping players: " + uuidSetToString(sleepingPlayers, server) + "\n" + "awake players: "
          + uuidSetToString(awakePlayers, server) + "\n" + "permasleep players: "
          + uuidSetToString(permaSleepPlayers, server) + "\n" + "initiator: " + initiator.getName().asString());


    if (percentYes >= requiredPercent) {
      sleep(server);
      cancelVoting();
      return true;
    }

    if (percentNo >= requiredPercent) {
      cancelVoting();
      return false;
    }
    return false;
  }

  private static void cancelVoting() {
    isVoting = false;
    awakePlayers = new HashSet<>();
    sleepingPlayers = new HashSet<>();
    initiator = null;
  }

  private boolean shouldCancelVoting(MinecraftServer server) {
    boolean somebodyIndBed = false;
    for (UUID uuid : sleepingPlayers) {
      PlayerEntity p = server.getPlayerManager().getPlayer(uuid);

      if (p.isSleepingLongEnough()) {
        somebodyIndBed = true;
        break;
      }
    }

    if (server.getWorld(World.OVERWORLD).isDay() && !somebodyIndBed) {
      return true;
    }
    return false;
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

  public static void playerClick(PlayerEntity player, ClickTypes vote) {
    System.out.println("here too");

    switch (vote) {
      case YES: {
        System.out.println("Clicked yes");
        vote(player, true, false);
        return;
      }
      case NO: {
        System.out.println("Clicked no");
        vote(player, false, false);
        return;
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

  public static void setPermaSleep(UUID playerUUID, boolean on) {
    if (on) {
      permaSleepPlayers.add(playerUUID);
    } else {
      permaSleepPlayers.remove(playerUUID);
    }
  }

  private static String uuidSetToString(Set<UUID> playerSet, MinecraftServer server) {
    List<String> nameList = new ArrayList<>();

    for (UUID uuid : playerSet) {
      nameList.add(server.getPlayerManager().getPlayer(uuid).getName().asString());
    }

    return "{" + String.join(", ", nameList) + "}";
  }
}
