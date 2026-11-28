package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSpellcasting.class)
public class DimmedMixin {

	@Inject(at = @At("HEAD"), method = "render")
	public void render(
		GuiGraphics ctx,
		int mouseX,
		int mouseY,
		float delta,
		CallbackInfo info
	) {
		if (Hexcessible.cfg().dimmed) ctx.fill(
			0,
			0,
			ctx.guiWidth(),
			ctx.guiHeight(),
			0x80000000
		);
	}
}
