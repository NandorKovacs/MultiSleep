package net.roaringmind.multisleep;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;

import org.apache.logging.log4j.Level;
import static net.minecraft.server.command.CommandManager.literal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;

public class MultiSleep implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "multisleep";
    public static final String MOD_NAME = "Multiplayer Sleep";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");

        registerCommands();
    }

    void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("vote").executes(context -> {
                MinecraftClient mc = MinecraftClient.getInstance();

                mc.openScreen(new CottonClientScreen(new SleepGUI()));

                return 1;
            }));

        });
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

}
