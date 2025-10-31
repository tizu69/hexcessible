package dev.tizu.hexcessible.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.MinecraftClient;

@Mixin(GuiSpellcasting.class)
public class PerWorldLearnMixin {
    @Inject(at = @At("HEAD"), method = "init")
    private void init(CallbackInfo info) {
        Hexcessible.LOGGER.debug("PerWorldLearnMixin.init");
        var scrollInOffhand = MinecraftClient.getInstance().player.getOffHandStack()
                .isOf(HexItems.SCROLL_LARGE);
        if (!scrollInOffhand)
            return;

        var scrollData = MinecraftClient.getInstance().player.getOffHandStack()
                .getNbt();
        if (scrollData == null)
            return;

        var angles = new ArrayList<HexAngle>();
        var anglesB = scrollData.getCompound("pattern").getByteArray("angles");
        for (var i = 0; i < anglesB.length; i++)
            angles.add(HexAngle.values()[anglesB[i]]);

        var id = scrollData.getString("op_id");
        var pat = PatternEntries.INSTANCE.get().stream()
                .filter(e -> e.id().toString().equals(id))
                .findFirst().orElse(null);
        if (pat == null || !pat.isPerWorld())
            return;

        PatternEntries.INSTANCE.setPerWorldSig(pat, angles);
    }
}