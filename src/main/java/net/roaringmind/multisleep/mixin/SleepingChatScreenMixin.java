package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.roaringmind.multisleep.callbacks.ExitBedCallback;

@Mixin(SleepingChatScreen.class)
public abstract class SleepingChatScreenMixin extends ChatScreen {

  public SleepingChatScreenMixin(String originalChatText) {
    super(originalChatText);
  }
  
  @Inject(method="stopSleeping()V", at = @At(value = "HEAD"))
  private void onExitBed() {
    ExitBedCallback.EVENT.invoker().interact(this.client.player);
  }
}
