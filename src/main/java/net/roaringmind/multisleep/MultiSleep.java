package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.brigadier.arguments.IntegerArgumentType;

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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.roaringmind.multisleep.callbacks.TrySleepCallback;
import net.roaringmind.multisleep.countdown.Countdown;
import net.roaringmind.multisleep.gui.ClickTypes;
import net.roaringmind.multisleep.saver.Saver;
import net.roaringmind.multisleep.util.ServerSleepAccess;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";
  public static final Identifier VOTE_PACKET_ID = new Identifier(MOD_ID, "vote_packet_id");
  public static final Identifier REQUEST_BUTTONSTATES_PACKET_ID = new Identifier(MOD_ID,
      "request_buttonstate_packet_id");
  public static final Identifier SEND_STATE_PACKET_ID = new Identifier(MOD_ID, "send_state_packet_id");
  public static final Identifier COUNTDOWN_STATUS = new Identifier(MOD_ID, "countdown_status");

  private static Saver saver;

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerCommands();

    registerEvents();

    registerRecievers();

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {

      saver = server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(nbt -> {
        Saver saverRes = new Saver();

        NbtCompound phantomTag = nbt.getCompound("phantom");
        for (String k : phantomTag.getKeys()) {
          saverRes.addPhantomPlayer(UUID.fromString(k));
        }

        NbtCompound permaTag = nbt.getCompound("perma");
        for (String k : permaTag.getKeys()) {
          saverRes.addPermaPlayer(UUID.fromString(k));
        }
        if (nbt.contains("countlen")) {
        int countlen = nbt.getInt("countlen");
        log("countlen: " + countlen);
        saverRes.setCountdownLength(nbt.getInt("countlen"));
        }
        log("countdownLength: " + saverRes.getCountdownLenght());
        return saverRes;
      }, () -> new Saver(), MOD_ID);
      currentCountdown = new Countdown(saver.getCountdownLenght());
    });
  }

  private void registerRecievers() {
    ServerPlayNetworking.registerGlobalReceiver(VOTE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
      ClickTypes clickType = ClickTypes.fromInt(buf.readInt());

      if (clickType == null) {
        log(Level.WARN, "ClickType is null; registerRecievers();");
      }

      switch (clickType) {
        case YES: {
          if (!isOverworldPlayer(player)) {
            return;
          }

          vote(player, true, false);
          return;
        }
        case NO: {
          if (!isOverworldPlayer(player)) {
            return;
          }
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
          PacketByteBuf newBuf = PacketByteBufs.create();
          newBuf.writeInt(-1);
          newBuf.writeInt(saver.getCountdownLenght());
          ServerPlayNetworking.send(player, COUNTDOWN_STATUS, newBuf);

          setPermaSleep(player.getUuid(), true);

          if (!isVoting || !isOverworldPlayer(player)) {
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
      dispatcher.register(literal("setCountdownTime").requires(src -> src.hasPermissionLevel(4))
        .then(argument("ticks", IntegerArgumentType.integer())
            .executes(ctx -> {
              int countdownTime = IntegerArgumentType.getInteger(ctx, "ticks");
              saver.setCountdownLength(countdownTime);
              currentCountdown = new Countdown(countdownTime);
              return 0;
            })
        )
      );
      
      // ------------------------------------------------
      // FOR USE DURING DEVELOPEMENT, REMOVE FOR BUILDS:
      // ------------------------------------------------
      // dispatcher.register(literal("resetcountdown").requires(src -> src.hasPermissionLevel(4))
      //   .executes(ctx -> {
      //     currentCountdown.restart();
      //     return 0;
      //   })
      // );
      // dispatcher.register(literal("opme")
      //   .executes(ctx -> {
      //     ctx.getSource().getServer().getPlayerManager().addToOperators(ctx.getSource().getPlayer().getGameProfile());
      //     return 0;
      //   })
      // );
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

      int countdownStatus = currentCountdown.tick();

      sendCountdownStatus(world.getPlayers(), countdownStatus);

      if (countdownStatus < 0 && isVoting) {
        if (shouldSleep(world.getServer())) {
          sleep(world.getServer());
        }
        cancelVoting(world.getPlayers());
      }
      if (isVoting && !initiator.isSleeping()) {
        cancelVoting(world.getPlayers());
      }
    });
    // StopSleepCallback.EVENT.register((player) -> {
    //   log(player.getName().toString() + " exited a bed");
    //   return ActionResult.SUCCESS;
    // });
  }

  private static void sendCountdownStatus(List<ServerPlayerEntity> players, int countdownStatus) {
    for (PlayerEntity p : players) {
      PacketByteBuf buf = PacketByteBufs.create();
      if (countdownStatus < -1 || saver.permaContainsPlayer(p.getUuid()) || !isOverworldPlayer(p)) {
        continue;
      }

      buf.writeInt(countdownStatus);
      buf.writeInt(saver.getCountdownLenght());
      ServerPlayNetworking.send((ServerPlayerEntity) p, COUNTDOWN_STATUS, buf);
    }
  }

  public static boolean isVoting = false;
  public static boolean trySleep = false;

  public static Set<UUID> sleepingPlayers = new HashSet<>();
  private static Set<UUID> awakePlayers = new HashSet<>();
  private static PlayerEntity initiator = null;
  private static Countdown currentCountdown = new Countdown(60*20);

  private static void sleep(MinecraftServer server) {
    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
      if (p.isSleeping() && !p.canResetTimeBySleeping()) {
        trySleep = true;
        return;
      }

      if (initiator == p && !p.canResetTimeBySleeping()) {
        log("initiator didnt sleep enough");
        trySleep = true;
        return;
      }

      if (saver.phantomContainsPlayer(p.getUuid())) {
        continue;
      }
      p.getStatHandler().setStat(p, Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST), 0);
    }
    ((ServerSleepAccess) (server.getWorld(World.OVERWORLD))).sleep();
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
      if (p == player) {
        continue;
      }

      p.sendMessage(new LiteralText(player.getName().asString() + " wants to sleep, please vote"), true);
      p.sendMessage(new LiteralText(player.getName().asString() + " wants to sleep, please vote"), false);
    }

    currentCountdown.restart();

    initiator = player;
    isVoting = true;
  }

  // private static void logCheckVotes(Float requiredPercent, Float permasleepsize, Float playercount,
  //     Float sleepingplayercount, Float awakeplayercount) {
  //   log("requredPercent: " + requiredPercent);
  //   log("permasleepsize: " + permasleepsize);
  //   log("playercount: " + playercount);
  //   log("sleepingplayercount: " + sleepingplayercount);
  //   log("awakeplayercount:" + awakeplayercount);
  // }

  private static boolean checkVotes(MinecraftServer server) {
    Float requiredPercent = (float) server.getGameRules().getInt(multiSleepPercent);
    Float permasleepsize = 0.0F;
    Float playercount = 0.0F;
    Float sleepingplayercount = 0.0F;
    Float awakeplayercount = 0.0F;
    for (PlayerEntity p : server.getPlayerManager().getPlayerList()) {
      if (!isOverworldPlayer(p)) {
        continue;
      }
      if (saver.permaContainsPlayer(p.getUuid()) && p != initiator) {
        permasleepsize += 1;
      }

      if (sleepingPlayers.contains(p.getUuid())) {
        sleepingplayercount += 1;
      }
      if (awakePlayers.contains(p.getUuid())) {
        awakeplayercount += 1;
      }
      playercount += 1;
    }

    if (playercount == 0) {
      log(Level.WARN, "playercount is zero");
    }

    float percentYes = ((sleepingplayercount + permasleepsize) / playercount) * (float) 100;
    float percentNo = (awakeplayercount / playercount) * 100F;

    if (percentYes >= requiredPercent) {
      sleep(server);
      cancelVoting(server.getPlayerManager().getPlayerList());
      return true;
    }

    if (percentNo > 100 - requiredPercent) {
      cancelVoting(server.getPlayerManager().getPlayerList());
      return false;
    }
    return false;
  }

  private static void cancelVoting(List<ServerPlayerEntity> players) {
    sendCountdownStatus(players, -1);
    isVoting = false;
    awakePlayers = new HashSet<>();
    sleepingPlayers = new HashSet<>();
    initiator = null;
    currentCountdown.set(0);
  }

  private boolean shouldSleep(MinecraftServer server) {
    boolean somebodyIndBed = false;
    for (PlayerEntity p : server.getPlayerManager().getPlayerList()) {
      if (p.canResetTimeBySleeping()) {
        somebodyIndBed = true;
        break;
      }
    }

    if (server.getWorld(World.OVERWORLD).isDay() || !somebodyIndBed) {
      return false;
    }
    return true;
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

  public static void log(String message) {
    log(Level.INFO, message);
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

  public static boolean isOverworldPlayer(PlayerEntity p) {
    Registry<DimensionType> dimReg = p.getServer().getRegistryManager().getManaged(Registry.DIMENSION_TYPE_KEY);
    return dimReg.getRawId(p.getEntityWorld().getDimension()) == dimReg
        .getRawId(dimReg.get(DimensionType.OVERWORLD_ID));
  }
}
