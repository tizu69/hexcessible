package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {
    private boolean shouldPresent = false;

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        Hexcessible.LOGGER.info("Now presenting interface");
        shouldPresent = true;
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        if (!shouldPresent)
            return;
        Hexcessible.LOGGER.info("Done presenting interface");
        shouldPresent = false;
    }
}