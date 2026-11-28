package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import dev.tizu.hexcessible.drawstate.Idling;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(KeyMapping.class)
public class NoHexicalEvokeMixin {
    @Unique
    private static final List<String> DISALLOWED = List.of(
            "key.hexical.evoke", "key.hexical.telepathy");

    // https://github.com/miyucomics/hexical/blob/main/src/client/java/miyucomics/hexical/inits/HexicalKeybinds.kt
    @Inject(method = "setDown", at = @At("HEAD"), cancellable = true)
    private void blockPressedWhileCasting(boolean pressed, CallbackInfo ci) {
        if (!Hexcessible.cfg().noHexicalEvoke)
            return;
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof GuiSpellcasting castui))
            return;
        var accessor = (DrawStateMixinAccessor) (Object) castui;
        if (accessor.state() instanceof Idling)
            return;
        KeyMapping self = (KeyMapping) (Object) this;
        String id = self.getName();
        if (DISALLOWED.contains(id) && pressed)
            ci.cancel();
    }
}
