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

    @Inject(at = @At("HEAD"), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        Hexcessible.LOGGER.info("Now presenting interface");
        shouldPresent = true;
    }

    @Inject(at = @At("HEAD"), method = "drawMove", remap = false)
    private void drawMove(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        if (!shouldPresent)
            return;
        Hexcessible.LOGGER.info("Done presenting interface (moved)");
        shouldPresent = false;
    }

    @Inject(at = @At("HEAD"), method = "drawEnd", remap = false)
    private void drawEnd(CallbackInfoReturnable<Boolean> info) {
        if (!shouldPresent)
            return;
        Hexcessible.LOGGER.info("Done presenting interface");
        shouldPresent = false;
    }
}