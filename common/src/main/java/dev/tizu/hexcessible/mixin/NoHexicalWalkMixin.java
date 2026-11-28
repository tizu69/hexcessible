package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(KeyMapping.class)
public class NoHexicalWalkMixin {
    @Unique
    private static final List<String> DISALLOWED = List.of("key.forward",
            "key.back", "key.left", "key.right", "key.jump", "key.sneak");

    // https://github.com/miyucomics/hexical/blob/main/src/client/java/miyucomics/hexical/mixin/ClientPlayerEntityMixin.java
    // This is sort of a hack, but it seems to work reasonably well. There might
    // be some better way to do this, but until someone comes up to me telling
    // me how shit this is, I'm just going to leave it like this.
    @Inject(method = "setDown", at = @At("HEAD"), cancellable = true)
    private void blockPressedWhileCasting(boolean pressed, CallbackInfo ci) {
        if (!Hexcessible.cfg().noHexicalWalk)
            return;
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof GuiSpellcasting))
            return;
        KeyMapping self = (KeyMapping) (Object) this;
        String id = self.getName();
        if (DISALLOWED.contains(id) && pressed)
            ci.cancel();
    }
}
