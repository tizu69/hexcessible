package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(GuiSpellcasting.class)
public class PerWorldLearnMixin {

	@Inject(at = @At("HEAD"), method = "init")
	private void init(CallbackInfo info) {
		Hexcessible.LOGGER.debug("PerWorldLearnMixin.init");
		var stack = Minecraft.getInstance().player.getOffhandItem();
		var scrollInOffhand = stack.is(HexItems.SCROLL_LARGE.get());
		if (!scrollInOffhand) return;

		var scrollData = stack.get(HexDataComponents.PATTERN.get());
		if (scrollData == null) return;
		var scrollAction = stack.get(HexDataComponents.ACTION.get());
		if (scrollAction == null) return;

		var angles = new ArrayList<HexAngle>();

		var anglesB = new ArrayList<HexAngle>();
		for (HexAngle next : scrollData.getSignature()) {
			anglesB.add(next);
		}

		for (var i = 0; i < anglesB.size(); i++) angles.add(anglesB.get(i));

		var pat = PatternEntries.INSTANCE.get()
			.stream()
			.filter(e ->
				e.id().toString().equals(scrollAction.location().toString())
			)
			.findFirst()
			.orElse(null);
		if (pat == null || !pat.isPerWorld()) return;

		PatternEntries.INSTANCE.setPerWorldSig(pat, angles);
	}
}
