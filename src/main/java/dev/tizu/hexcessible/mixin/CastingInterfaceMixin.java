package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {
    private boolean shouldPresent = false;
    private String query = "";

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        shouldPresent = true;
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        if (!shouldPresent)
            return;
        shouldPresent = false;
    }

    @Inject(at = @At("RETURN"), method = "render", remap = false)
    private void render(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        if (!shouldPresent)
            return;
        var tr = MinecraftClient.getInstance().textRenderer;

        var text = !query.equals("") ? Text.literal(query)
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, text, mouseX, mouseY);
    }
}