package net.roaringmind.multisleep.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.roaringmind.multisleep.callbacks.ButtonClickCallback;

@Mixin(InventoryScreen.class)
public abstract class InventoryButtonMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private static final Identifier RECIPE_BUTTON_TEXTURE2 = new Identifier("textures/gui/recipe_button.png");
    private TexturedButtonWidget myButton;

    InventoryButtonMixin() {
        super(null, null, null);
    }

    @Inject(method = "init", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;addButton(Lnet/minecraft/client/gui/widget/AbstractButtonWidget;)Lnet/minecraft/client/gui/widget/AbstractButtonWidget;"))
    void addCustomButton(final CallbackInfo info) {
        System.out.println("XXXXXX button width = " + this.width);

        myButton = new TexturedButtonWidget(this.x + 125, this.height / 2 - 22, 20, 18, 0, 0, 19,
                RECIPE_BUTTON_TEXTURE2, (buttonWidget) -> {

                  ActionResult result = ButtonClickCallback.EVENT.invoker().interact(this.client.player);

                    if (result == ActionResult.SUCCESS) {
                      System.out.println("Clicked the Button");
                    }
                  });
        this.addButton(myButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    void onRender(final MatrixStack matrices, int mouseX, int mouseY, float delta, final CallbackInfo info) {
        myButton.setPos(this.x + 125, this.height / 2 - 22);
    }
}
