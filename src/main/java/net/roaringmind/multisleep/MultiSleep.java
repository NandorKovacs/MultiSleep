package net.roaringmind.multisleep;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class MultiSleep implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "multisleep";
  public static final String MOD_NAME = "Multiplayer Sleep";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");

    registerCommands();
  }

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("multisleep")
        .then(literal("forcesleep")
          .executes(ctx -> {
            sleep();
            return 0;
          })  
        )
      );
    });
  }

  public static boolean shoudlSleepNow = false;

  private void sleep() {
    shoudlSleepNow = true;
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }
}
