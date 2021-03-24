package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MultiSleepClient implements ClientModInitializer {

  public static final Identifier OPEN_GUI_PACKET_ID = new Identifier("multisleep", "open_gui");

  @Override
  public void onInitializeClient() {
    ClientPlayConnectionEvents.INIT.register((handler, client) -> {
      ClientPlayNetworking.registerReceiver(OPEN_GUI_PACKET_ID, MultiSleepClient::openGUI);
    });
  }

  public static void openGUI(MinecraftClient mc, ClientPlayNetworkHandler handler, PacketByteBuf packetByteBuf, PacketSender sender) {

    mc.openScreen(new CottonClientScreen(new SleepGUI(packetByteBuf.readBoolean())));
  }

}
