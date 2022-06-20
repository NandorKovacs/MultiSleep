package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;
import net.roaringmind.multisleep.callbacks.StopSleepCallback;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class StopSleepMixin {

  @Inject(method = "onClientCommand(Lnet/minecraft/network/packet/c2s/play/ClientCommandC2SPacket;)V", at = @At(value = "INVOKE", target="Lnet/minecraft/server/network/ServerPlayerEntity;isSleeping()Z"))
  private void onStopSleep(final ClientCommandC2SPacket packet, final CallbackInfo info) {
    ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
    
    ActionResult result = StopSleepCallback.EVENT.invoker().interact(handler.player);
  }
}
