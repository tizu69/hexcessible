package dev.tizu.hexcessible.mixin;

import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class DrawStateScreenMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers,
            CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DrawStateMixinAccessor accessor) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE)
                accessor.state().requestExit();
            else
                accessor.state().onKeyPress(keyCode, modifiers);
            cir.setReturnValue(true);
        }
    }
}
