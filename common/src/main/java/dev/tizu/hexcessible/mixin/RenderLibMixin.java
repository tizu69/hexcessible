package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.client.render.RenderLib;
import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(RenderLib.class)
public class RenderLibMixin {

    @Inject(method = "makeZappy", at = @At("HEAD"), remap = false, cancellable = true)
    private static void prefersReducedZappiness(List<Vec2> barePoints, Set<Integer> dupIndices, int hops, float variance, float speed, float flowIrregular, float readabilityOffset, float lastSegmentLenProportion, double seed, CallbackInfoReturnable<List<Vec2>> cir) {
        if (Hexcessible.cfg().prefersReducedMotion) {
            cir.setReturnValue(barePoints);
        }
    }

}
