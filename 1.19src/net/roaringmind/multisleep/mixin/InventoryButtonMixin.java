package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.roaringmind.multisleep.MultiSleep;

@Mixin(InventoryScreen.class)
public abstract class InventoryButtonMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
  private static final Identifier SLEEP_BUTTON = new Identifier(MultiSleep.MOD_ID, "textures/gui/sleep_button.png");
  private TexturedButtonWidget myButton;

  InventoryButtonMixin() {
    super(null, null, null);
  }

  @Inject(method = "init", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screen/recipebook/RecipeBookWidget;findLeftEdge(II)I"))
  void addCustomButton(final CallbackInfo info) {
    myButton = new TexturedButtonWidget(this.x + 150, this.height / 2 - 22, 20, 18, 0, 0, 19, SLEEP_BUTTON,
        buttonWidget -> {
          ClientPlayNetworking.send(MultiSleep.REQUEST_BUTTONSTATES_PACKET_ID, PacketByteBufs.create());
        });
    this.addDrawableChild(myButton);
  }

  @Inject(method = "render", at = @At("HEAD"))
  void onRender(final MatrixStack matrices, int mouseX, int mouseY, float delta, final CallbackInfo info) {
    myButton.setPos(this.x + 150, this.height / 2 - 22);
  }
}
