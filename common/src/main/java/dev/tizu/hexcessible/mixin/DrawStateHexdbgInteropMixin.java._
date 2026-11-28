package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import dev.tizu.hexcessible.drawstate.DrawState;
import gay.object.hexdebug.gui.splicing.SplicingTableScreen;
import net.minecraft.client.gui.DrawContext;

@Mixin(SplicingTableScreen.class)
public class DrawStateHexdbgInteropMixin {
    @Unique
    private DrawState state() {
        var self = (SplicingTableScreen) (Object) this;
        var castui = (DrawStateMixinAccessor) (Object) self.getGuiSpellcasting();
        castui.disallowTyping();
        return castui.state();
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext ctx, int mx, int my, float delta, CallbackInfo info) {
        state().onRender(ctx, mx, my);
    }

    /*
     * @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
     * public void keyPressed(int keyCode, int scanCode, int modifiers,
     * CallbackInfoReturnable<Boolean> info) {
     * state().onKeyPress(keyCode, modifiers);
     * if (!(state() instanceof Idling)) {
     * if (keyCode == GLFW.GLFW_KEY_ESCAPE)
     * state().requestExit();
     * info.setReturnValue(true);
     * }
     * }
     */
}
