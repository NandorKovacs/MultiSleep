package net.roaringmind.multisleep;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import net.roaringmind.multisleep.gui.SleepGUI;

public class ClientMultiSleep implements ClientModInitializer {
  private static KeyBinding guiKeyBinding;
  private static KeyBinding voteYesKeyBinding;
  private static KeyBinding voteNoKeyBinding;

  @Override
  public void onInitializeClient() {
    registerKeyBinds();

    registerEvents();

    registerRecievers();
  }

  private static int countdownStatus = -1;
  private static int previousCountdown = 0;

  private void registerEvents() {
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

    HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
      if (countdownStatus <= -1 && previousCountdown <= 0) {
        return;
      }
      int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
      int progress = countdownStatus;
      if (countdownStatus <= -1) {
        progress = previousCountdown;
      }

      int color = MathHelper.packRgb(138, 43, 226) + (255 << 24);
      DrawableHelper.fill(matrixStack, 0, 0, width, 10, 150 << 24);
      DrawableHelper.fill(matrixStack, 0, 0, progress, 10, color);

      DrawableHelper.fill(matrixStack, 0, 0, progress, 10, 255);
      previousCountdown = progress;
      countdownStatus = -1;
    });
  }

  private void registerKeyBinds() {
    guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.opengui", InputUtil.Type.KEYSYM,
        InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
    voteYesKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.voteyes",
        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
    voteNoKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.multisleep.voteno",
        InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.multisleep.keybinds"));
  }

  private void registerRecievers() {
    ClientPlayNetworking.registerGlobalReceiver(MultiSleep.SEND_STATE_PACKET_ID,
        (client, handler, buf, responseSender) -> {
          int[] states = buf.readIntArray();

          client.execute(() -> MinecraftClient.getInstance()
              .setScreen(new CottonClientScreen(new SleepGUI(intToBool(states[0]), intToBool(states[1])))));

        });

    ClientPlayNetworking.registerGlobalReceiver(MultiSleep.COUNTDOWN_STATUS, (client, handler, buf, responseSender) -> {
      int bufInt = buf.readInt();
      int countdownLength = buf.readInt();
      if (bufInt <= -1) {
        countdownStatus = -1;
        previousCountdown = -1;
        return;
      }

      countdownStatus = (int) (((float) client.getWindow().getScaledWidth() / (float) countdownLength)
          * (float) bufInt);
    });
  }

  private boolean intToBool(int n) {
    if (n == 0) {
      return false;
    }
    return true;
  }
}
