package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerEvents();

    registerCommands();
  }

  public List<AbstractClientPlayerEntity> getPlayers() {
    return MinecraftClient.getInstance().world.getPlayers();
  }

  public void broadcast(String message) {
    for (AbstractClientPlayerEntity player : getPlayers()) {
      player.sendMessage(Text.of(message), true);
    }
  }

  Key<IntRule> multisleeppercent_key;

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

        mc.openScreen(new CottonClientScreen(new SleepGUI(this)));

        return 1;
      }));

      dispatcher.register(literal("broadcast").executes(context -> {
        broadcast("hello");

        return 1;
      }));

    });
  }

  void registerEvents() {
    PlayerSleepCallback.EVENT.register((player, pos) -> {

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

      mc.openScreen(new CottonClientScreen(new SleepGUI(this)));
      return ActionResult.SUCCESS;
    });
  }

  public Set<PlayerEntity> afkPlayers;
  public Set<PlayerEntity> votedYes;
  public Set<PlayerEntity> votedNo;
  public boolean voting = false;

  public void startVoting(PlayerEntity initiator) {
    if (getPlayers().size() == 1) {
      broadcast("Sleep tight" + initiator.getName());
      return;
    }

    broadcast("Voting for Sleep, Voting started by " + initiator.getName());
    voting = true;
    for (AbstractClientPlayerEntity player : getPlayers()) {
      if (afkPlayers.contains(player)) {
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
    if (sleep) {
      votedYes.add(player);
    } else {
      int percent = player.getServer().getGameRules().getInt(multisleeppercent_key);
      if (percent == 0) {
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
    stopVoting();
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }
}
