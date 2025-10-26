package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import at.petrak.hexcasting.api.client.ClientRenderHelper;
import dev.tizu.hexcessible.HexcessibleConfig;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(ClientRenderHelper.class)
public class FloatiesMixin {
    private FloatiesMixin() {
    }

    @WrapMethod(method = "renderCastingStack", remap = false)
    private static void renderCastingStack(MatrixStack ps, PlayerEntity player, float pticks,
            Operation<Void> original) {
        if (!HexcessibleConfig.get().hideFloaties)
            original.call(ps, player, pticks);
    }
}