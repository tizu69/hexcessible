package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.HexcessibleConfig;
import net.minecraft.client.gui.DrawContext;

@Mixin(GuiSpellcasting.class)
public class DimmedMixin {

    @Inject(at = @At("HEAD"), method = "render", remap = false)
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        if (HexcessibleConfig.get().dimmed)
            ((GuiSpellcasting) (Object) this).renderBackground(ctx);
    }
}