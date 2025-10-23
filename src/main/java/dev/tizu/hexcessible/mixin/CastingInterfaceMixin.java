package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.HexcessibleState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        HexcessibleState.shouldPresent = true;
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        if (!HexcessibleState.shouldPresent)
            return;
        HexcessibleState.shouldPresent = false;
    }

    @Inject(at = @At("RETURN"), method = "render", remap = false)
    private void render(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        if (!HexcessibleState.shouldPresent)
            return;
        var tr = MinecraftClient.getInstance().textRenderer;

        var text = !HexcessibleState.query.equals("")
                ? Text.literal(HexcessibleState.query)
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, text, mouseX, mouseY);
    }
}