package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class ExampleClientMixin {
    @Inject(at = @At("HEAD"), method = "run")
    private void init(CallbackInfo info) {
        Hexcessible.LOGGER.info("This line is printed by an example mod mixin!");
    }
}