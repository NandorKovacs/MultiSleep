package net.roaringmind.multisleep.mixin;

import com.mojang.datafixers.util.Either;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.roaringmind.multisleep.callbacks.TrySleepCallback;

@Mixin(PlayerEntity.class)
public abstract class TrySleepMixin extends LivingEntity {

  TrySleepMixin() {
    super(null, null);
  }
  
  @Inject(method = "trySleep", at = @At(value = "HEAD"), cancellable = true)
  private void onTrySleep(final BlockPos pos, final CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> info) {

    PlayerEntity player = (PlayerEntity) (Object) this;

    ActionResult result = TrySleepCallback.EVENT.invoker().interact(player, pos);

    if (result == ActionResult.FAIL) {
      player.sendMessage(Text.of("oh oh, somethings wrong"), true);
      info.setReturnValue(Either.left(PlayerEntity.SleepFailureReason.OTHER_PROBLEM));
    }
  }
}
