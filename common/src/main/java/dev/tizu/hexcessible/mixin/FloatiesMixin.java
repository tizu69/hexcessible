package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.api.client.ClientRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientRenderHelper.class)
public class FloatiesMixin {
    private FloatiesMixin() {
    }

    @Inject(method = "renderCastingStack", at = @At("HEAD"), remap = false, cancellable = true)
    private static void renderCastingStack(PoseStack ps, Player player, float pticks, CallbackInfo ci) {
        if (Hexcessible.cfg().hideFloaties) {
            ci.cancel();
        }
    }
}