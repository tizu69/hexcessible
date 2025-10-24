package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.AutocompleteProvider;
import net.minecraft.client.gui.DrawContext;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        AutocompleteProvider.INSTANCE.startPresenting((int) mxOut, (int) myOut);
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        AutocompleteProvider.INSTANCE.stopPresenting();
    }

    @Inject(at = @At("RETURN"), method = "render", remap = false)
    public void onRender(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        AutocompleteProvider.INSTANCE.onRender(ctx, mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        AutocompleteProvider.INSTANCE.onKeyPress(keyCode, modifiers,
                ((GuiSpellcasting) (Object) this)::close);
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        AutocompleteProvider.INSTANCE.onCharType(chr);
        return true;
    }
}