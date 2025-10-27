package dev.tizu.hexcessible.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;

@Mixin(ClientPlayerEntity.class)
public class DrawStateClientPlayerEntityMixin {
    @Shadow
    public Input input;

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onMovement(Lnet/minecraft/client/input/Input;)V", shift = At.Shift.AFTER))
    private void noHexicalWalk(CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof GuiSpellcasting))
            return;

        // TODO: this prevents Hexical walk from working, as it collides with
        // our keyboard input method. We might want to implement a way to handle
        // walking for the players who want to walk?
        input.pressingForward = false;
        input.pressingBack = false;
        input.pressingLeft = false;
        input.pressingRight = false;
        input.jumping = false;
        input.sneaking = false;
    }

}
