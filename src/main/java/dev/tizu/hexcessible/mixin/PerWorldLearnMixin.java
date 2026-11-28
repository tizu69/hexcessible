package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.entries.PatternEntries;
import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSpellcasting.class)
public class PerWorldLearnMixin {

	@Inject(at = @At("HEAD"), method = "init")
	private void init(CallbackInfo info) {
		Hexcessible.LOGGER.debug("PerWorldLearnMixin.init");
		var stack = MinecraftClient.getInstance().player.getOffHandStack();
		var scrollInOffhand = stack.isOf(HexItems.SCROLL_LARGE.get());
		if (!scrollInOffhand) return;

		var scrollData = stack.get(HexDataComponents.PATTERN.get());
		if (scrollData == null) return;
		var scrollAction = stack.get(HexDataComponents.ACTION.get());
		if (scrollAction == null) return;

		var angles = new ArrayList<HexAngle>();
		var anglesB = scrollData.getAngles();
		for (var i = 0; i < anglesB.size(); i++) angles.add(anglesB.get(i));

		var pat = PatternEntries.INSTANCE.get()
			.stream()
			.filter(e ->
				e.id().toString().equals(scrollAction.getValue().toString())
			)
			.findFirst()
			.orElse(null);
		if (pat == null || !pat.isPerWorld()) return;

		PatternEntries.INSTANCE.setPerWorldSig(pat, angles);
	}
}
