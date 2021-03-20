package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.roaringmind.multisleep.callbacks.PlayerTickCallback;

@Mixin(PlayerEntity.class)
public class PlayerTickMixin {
  PlayerEntity self = (PlayerEntity) (Object) this;

  @Inject(method = "tick", at = @At("HEAD"))
  public void searchAFK(final CallbackInfo info) {
    ActionResult result = PlayerTickCallback.EVENT.invoker().interact((PlayerEntity) (Object) this);
    if (result != ActionResult.SUCCESS) {
      self.sendMessage(Text.of("No longer AFK"), true);
    }
  }
}
