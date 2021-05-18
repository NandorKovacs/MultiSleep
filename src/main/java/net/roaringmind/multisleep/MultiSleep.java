package net.roaringmind.multisleep;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
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
  }

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
            vote(ctx.getSource().getPlayer());
            return 0;
          })
        )
      );
    });
  }
  //@formatter:on

  private void registerEvents() {
    TrySleepCallback.EVENT.register((player, pos) -> {
      vote(player);
      return ActionResult.PASS;
    });
  }

  public static boolean shouldSleepNow = false;
  public boolean isVoting = false;

  private List<UUID> sleepingPlayers;

  private void sleep() {
    shouldSleepNow = true;
  }

  private void vote(PlayerEntity player) {
    if (!isVoting) {
      startVoting(player);
    }
    sleepingPlayers.add(player.getUuid());
  }

  private void startVoting(PlayerEntity player) {
    for (PlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
      p.sendMessage(new LiteralText(player.getName() + " wants to sleep, please vote"), true);
      p.sendMessage(new LiteralText(player.getName() + " wants to sleep, please vote"), false);
    }
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

  public static void playerClick(PlayerEntity player, ClickTypes vote) {
    
  }
}
