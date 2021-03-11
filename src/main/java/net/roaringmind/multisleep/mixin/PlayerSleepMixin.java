package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.roaringmind.multisleep.PlayerSleepCallback;

@Mixin(BedBlock.class)
public abstract class PlayerSleepMixin extends HorizontalFacingBlock {

  PlayerSleepMixin() {
    super(null);
  }

  @Inject(method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;trySleep(Lnet/minecraft/util/math/BlockPos;)Lcom/mojang/datafixers/util/Either;", args = {
      "log=true" }), cancellable = true)
  private void onTrySleep(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player,
      final Hand hand, final BlockHitResult hit, final CallbackInfoReturnable<ActionResult> info) {

        ActionResult result = PlayerSleepCallback.EVENT.invoker().interact(player, (BedBlock)(Object)this);

        if (result == ActionResult.FAIL) {
          info.cancel();
        }
    /*
     * System.out.println("XXXXX sleep");
     *
     * ItemStack stack = new ItemStack(Items.DIAMOND); ItemEntity itemEntity = new
     * ItemEntity(player.world, pos.getX(), pos.getY(), pos.getZ(), stack);
     * player.world.spawnEntity(itemEntity);
     *
     * info.setReturnValue(ActionResult.SUCCESS);
     */
  }
}
