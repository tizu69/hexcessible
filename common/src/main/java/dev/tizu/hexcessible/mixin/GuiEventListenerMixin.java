package dev.tizu.hexcessible.mixin;

import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface GuiEventListenerMixin {

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void onCharTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DrawStateMixinAccessor accessor) {
            accessor.state().onCharType(chr);
            cir.setReturnValue(true);
        }
    }
}
