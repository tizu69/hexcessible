package dev.tizu.hexcessible.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import dev.tizu.hexcessible.drawstate.Idling;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec2f;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.gui.GuiBook;

@Mixin(Screen.class)
public class KeyDocsScreenMixin {
    @Unique
    private static GuiSpellcasting staffScreen;
    @Unique
    private static Vec2f mousePos = new Vec2f(0, 0);

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    void returnToStaff(CallbackInfo ci) {
        if ((Screen) (Object) this instanceof GuiBook && staffScreen != null) {
            MinecraftClient.getInstance().setScreen(staffScreen);
            staffScreen = null;
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    void render(DrawContext ctx, int mx, int my, float delta, CallbackInfo info) {
        if ((Object) this instanceof DrawStateMixinAccessor)
            mousePos = new Vec2f((float) mx, (float) my);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), order = 999)
    void openHexbook(int keycode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof DrawStateMixinAccessor accessor
                && accessor.state() instanceof Idling
                && Hexcessible.cfg().keyDocs
                && keycode == GLFW.GLFW_KEY_N) {
            staffScreen = (GuiSpellcasting) (Object) this;
            var pos = accessor.getPatternAt((int) mousePos.x, (int) mousePos.y);
            if (!openHexbookEntry(pos, keycode, modifiers))
                PatchouliAPI.get().openBookGUI(BookEntries.BOOKID);
        }
    }

    @Unique
    boolean openHexbookEntry(HexPattern pat, int keycode, int modifiers) {
        if (pat == null)
            return false;
        var ptrn = PatternEntries.INSTANCE.getFromSig(pat.anglesSignature());
        if (ptrn == null)
            return false;
        var entry = BookEntries.INSTANCE.getBookEntryFor(ptrn.id().toString());
        if (entry == null)
            return false;
        PatchouliAPI.get().openBookEntry(BookEntries.BOOKID,
                entry.entryid(), entry.page());
        return true;
    }
}
