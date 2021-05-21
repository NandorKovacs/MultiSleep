package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import it.unimi.dsi.fastutil.ints.IntIterable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.roaringmind.multisleep.gui.SleepGUI;

public class ClientMultiSleep implements ClientModInitializer {
  private static KeyBinding guiKeyBinding;
  private static KeyBinding voteYesKeyBinding;
  private static KeyBinding voteNoKeyBinding;

  @Override
  public void onInitializeClient() {
    registerKeyBinds();

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (guiKeyBinding.wasPressed()) {
        ClientPlayNetworking.send(MultiSleep.REQUEST_BUTTONSTATES_PACKET_ID, PacketByteBufs.create());
      }
      
      while (voteYesKeyBinding.wasPressed()) {
        MultiSleep.vote(client.player, true, false);
      }
      while (voteNoKeyBinding.wasPressed()) {
        MultiSleep.vote(client.player, false, false);
      }
    });
  
    ClientPlayNetworking.registerGlobalReceiver(MultiSleep.SEND_STATE_PACKET_ID, (client, handler, buf, responseSender) -> {
      int[] states = buf.readIntArray();
      MinecraftClient.getInstance().openScreen(new CottonClientScreen(new SleepGUI(intToBool(states[0]), intToBool(states[1]))));
    });
  }

  private boolean intToBool(int n) {
    if (n == 0) {
      return false;
    }
    return true;
  }

  private static void registerKeyBinds() {
    guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.opengui", InputUtil.Type.KEYSYM,
        InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
    voteYesKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.voteyes",
        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
    voteNoKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.voteno",
        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
  }

}
