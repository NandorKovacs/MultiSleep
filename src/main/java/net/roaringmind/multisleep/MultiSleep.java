package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;
import net.roaringmind.multisleep.callbacks.ButtonClickCallback;
import net.roaringmind.multisleep.callbacks.PlayerSleepCallback;
import net.roaringmind.multisleep.callbacks.PlayerTickCallback;
import net.roaringmind.multisleep.callbacks.WorldSleepCallback;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";
  public static final Identifier TIME_SINCE_SLEPT_IN_BED = new Identifier("multisleep", "time_since_last_slept_in_bed");
  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerStats();

    registerEvents();

    registerCommands();
  }

  public List<AbstractClientPlayerEntity> getPlayers() {
    return MinecraftClient.getInstance().world.getPlayers();
  }

  public void broadcast(String message, boolean toolbar) {
    for (AbstractClientPlayerEntity player : getPlayers()) {
      player.sendMessage(Text.of(message), toolbar);
      ;
    }
  }

  Key<IntRule> multisleeppercent_key;

  void registerStats() {
    Registry.register(Registry.CUSTOM_STAT, "time_since_last_slept_in_bed", TIME_SINCE_SLEPT_IN_BED);
    Stats.CUSTOM.getOrCreateStat(TIME_SINCE_SLEPT_IN_BED, StatFormatter.TIME);
  }

  void registerCommands() {
    if (GameRuleRegistry.hasRegistration("MultiSleepPercent")) {
      log(Level.WARN,
          "Can't register gamerule, other gamerule with the same name is already existing. please remove incompatible mods to be able to use the gamerule \"MultiSleepPercent\"");
    } else {
      multisleeppercent_key = GameRuleRegistry.register("MultiSleepPercent", Category.MISC,
          GameRuleFactory.createIntRule(0, 0, 100));
    }
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("vote").executes(context -> {
        MinecraftClient mc = MinecraftClient.getInstance();

        mc.openScreen(new CottonClientScreen(new SleepGUI(this, wants_phantoms.get(mc.player))));

        return 1;
      }));

      dispatcher.register(literal("broadcast").executes(context -> {
        broadcast("hello", true);

        return 1;
      }));

    });
  }

  void registerEvents() {
    PlayerSleepCallback.EVENT.register((player, pos) -> {
      if (voting) {
        vote(true, player);
        return ActionResult.PASS;
      }
      startVoting(player);
      return ActionResult.PASS;
    });
    WorldSleepCallback.EVENT.register(() -> {
      if (sleepNow) {
        sleepNow = false;
        return ActionResult.SUCCESS;
      }
      return ActionResult.PASS;
    });
    ButtonClickCallback.EVENT.register((player) -> {
      MinecraftClient mc = MinecraftClient.getInstance();

      mc.openScreen(new CottonClientScreen(new SleepGUI(this, wants_phantoms.get(mc.player))));
      return ActionResult.SUCCESS;
    });
    PlayerTickCallback.EVENT.register((player) -> {
      UUID name = player.getUuid();
      if (afkPlayers.containsKey(name) && !afkPlayers.get(name).check()) {
        afkPlayers.remove(name);
        return ActionResult.FAIL;
      }
      return ActionResult.PASS;
    });
    ClientPlayConnectionEvents.JOIN.register((handler, server, client) -> {
      wants_phantoms.put(client.player, false);
    });
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      wants_phantoms.remove(client.player);
    });
  }

  public HashMap<PlayerEntity, Boolean> wants_phantoms = new HashMap<>();
  public HashMap<UUID, AFKPlayer> afkPlayers = new HashMap<>();
  public Set<PlayerEntity> votedYes;
  public Set<PlayerEntity> votedNo;
  public boolean voting = false;

  public void startVoting(PlayerEntity initiator) {
    if (getPlayers().size() == 1) {
      broadcast("Sleep tight" + initiator.getName(), true);
      return;
    }

    broadcast("Voting for Sleep, Voting started by " + initiator.getName(), true);
    voting = true;
    for (AbstractClientPlayerEntity player : getPlayers()) {
      if (afkPlayers.containsKey(player.getUuid())) {
        votedYes.add(player);
      }
    }
  }

  public void stopVoting() {
    voting = false;
    votedYes.clear();
    votedNo.clear();
  }

  public void checkVotes(PlayerEntity player) {
    int playerCount = getPlayers().size();
    int percent = player.getServer().getGameRules().getInt(multisleeppercent_key);
    if (votedYes.size() / playerCount * 100 < percent) {
      sleep();
      return;
    }
    if (votedNo.size() / playerCount * 100 < percent) {
      stopVoting();
      return;
    }
  }

  public void vote(boolean sleep, PlayerEntity player) {
    int percent = player.getServer().getGameRules().getInt(multisleeppercent_key);
    if (sleep) {
      if (percent == 0) {
        sleep();
        return;
      }
      votedYes.add(player);
    } else {
      if (percent == 100) {
        stopVoting();
        return;
      } else {
        votedNo.add(player);
      }
    }
    checkVotes(player);
  }

  public boolean sleepNow = false;

  public void sleep() {
    sleepNow = true;
    for (AbstractClientPlayerEntity p : getPlayers()) {
      if (!wants_phantoms.get(p)) {
        p.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
      }
    }
    stopVoting();
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }
}
