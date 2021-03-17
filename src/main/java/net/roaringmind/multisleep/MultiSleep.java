package net.roaringmind.multisleep;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;

import javax.lang.model.element.TypeElement;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

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

  void registerCommands() {
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
      System.out.println("XXXXX sleep");

      /*ItemStack stack = new ItemStack(Items.DIAMOND);
      ItemEntity itemEntity = new ItemEntity(player.world, pos.getX(), pos.getY(), pos.getZ(), stack);
      player.world.spawnEntity(itemEntity);
      */

      startVoting(player);

      return ActionResult.PASS;
    });
  }

  public void startVoting(PlayerEntity initiator) {
    broadcast("Voting for Sleep, Voting started by " + initiator.getName());
    for (AbstractClientPlayerEntity player : getPlayers()) {
    }
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
