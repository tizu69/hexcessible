package dev.tizu.hexcessible.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.CastingInterfaceAccessor;
import dev.tizu.hexcessible.autocomplete.AutocompleteProvider;
import dev.tizu.hexcessible.inspect.InspectProvider;
import net.minecraft.client.gui.DrawContext;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {
    CastingInterfaceAccessor castui;
    AutocompleteProvider autocompleteProvider;
    InspectProvider inspectProvider;

    @Inject(at = @At("HEAD"), method = "init", remap = false)
    private void init(CallbackInfo info) {
        castui = new CastingInterfaceAccessor((GuiSpellcasting) (Object) this);
        autocompleteProvider = new AutocompleteProvider();
        inspectProvider = new InspectProvider(castui);
    }

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        autocompleteProvider.startPresenting((int) mxOut, (int) myOut);
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        autocompleteProvider.stopPresenting();
        inspectProvider.onMouseMove();
    }

    @Inject(at = @At("RETURN"), method = "render", remap = false)
    public void onRender(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        autocompleteProvider.onRender(ctx, mouseX, mouseY);
        inspectProvider.onRender(ctx, mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return autocompleteProvider.onKeyPress(keyCode, modifiers)
                || inspectProvider.onKeyPress(keyCode, modifiers)
                || keyPressedClose(keyCode);
    }

    private boolean keyPressedClose(int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE)
            return false;
        ((GuiSpellcasting) (Object) this).close();
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        return autocompleteProvider.onCharType(chr)
                || inspectProvider.onCharType(chr);
    }
}