package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;

@Mixin(GuiSpellcasting.class)
public class ShowAllDotsMixin {
    // TODO: make this more performant, then reintroduce

    @ModifyVariable(at = @At("STORE"), method = "render", name = "radius")
    public int radius(int radius) {
        return Hexcessible.cfg().showAllDots ? 50 : 3;
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lat/petrak/hexcasting/api/casting/math/HexCoord;rangeAround(I)Ljava/util/Iterator;", shift = At.Shift.AFTER), method = "render", name = "radius", remap = false)
    public int radius$reset(int radius) {
        return 3;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F"), method = "render", index = 1)
    public float scaledDist(float scaledDist) {
        return Hexcessible.cfg().showAllDots ? 0.5f : 0f;
    }
}