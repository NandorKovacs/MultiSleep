package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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
        client.openScreen(new CottonClientScreen(new SleepGUI()));
      }
      while (voteYesKeyBinding.wasPressed()) {
        MultiSleep.vote(client.player, true, false);
      }
      while (voteNoKeyBinding.wasPressed()) {
        MultiSleep.vote(client.player, false, false);
      }
    });
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
