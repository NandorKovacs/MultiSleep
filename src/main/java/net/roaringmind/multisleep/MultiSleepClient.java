package net.roaringmind.multisleep;

import com.google.common.collect.Multiset;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MultiSleepClient implements ClientModInitializer {

  public static final Identifier OPEN_GUI_PACKET_ID = new Identifier("multisleep", "open_gui");

  @Override
  public void onInitializeClient() {
      }

  void registerEvents() {
    ClientPlayConnectionEvents.INIT.register((handler, client) -> {
      ClientPlayNetworking.registerReceiver(OPEN_GUI_PACKET_ID, MultiSleepClient::openGUI);
    });
  
    ClientPlayConnectionEvents.JOIN.register((handler, server, client) -> {
      PacketByteBuf isjoin = PacketByteBufs.create();
      isjoin.writeBoolean(true);
      ClientPlayNetworking.send(MultiSleep.CLIENT_CONNECTION_PACKET, isjoin);
    });
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      PacketByteBuf isjoin = PacketByteBufs.create();
      isjoin.writeBoolean(false);
      ClientPlayNetworking.send(MultiSleep.CLIENT_CONNECTION_PACKET, isjoin);
    });
  }

  public static void openGUI(MinecraftClient mc, ClientPlayNetworkHandler handler, PacketByteBuf packetByteBuf, PacketSender sender) {
    mc.openScreen(new CottonClientScreen(new SleepGUI(packetByteBuf.readBoolean())));
  }

}
