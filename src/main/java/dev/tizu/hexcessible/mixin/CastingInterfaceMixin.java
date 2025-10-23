package dev.tizu.hexcessible.mixin;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.HexcessibleState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(GuiSpellcasting.class)
public class CastingInterfaceMixin {

    @Inject(at = @At(value = "INVOKE", target = "play", shift = At.Shift.BEFORE), method = "drawStart", remap = false)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> info) {
        HexcessibleState.setShouldPresent(true);
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved", remap = false)
    private void mouseMoved(CallbackInfo info) {
        if (!HexcessibleState.getShouldPresent())
            return;
        HexcessibleState.setShouldPresent(false);
    }

    @Inject(at = @At("RETURN"), method = "render", remap = false)
    private void render(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        if (!HexcessibleState.getShouldPresent())
            return;
        var tr = MinecraftClient.getInstance().textRenderer;

        var text = !HexcessibleState.getQuery().equals("")
                ? Text.literal(HexcessibleState.getQuery())
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, text, mouseX, mouseY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                var q = HexcessibleState.getQuery();
                if (ctrl) { // remove last word
                    var words = q.split(" ");
                    var output = Arrays.stream(words)
                            .limit(words.length - 1l)
                            .collect(Collectors.joining(" "));
                    HexcessibleState.setQuery(output);
                } else { // remove single character
                    HexcessibleState.setQuery(q.isEmpty() ? ""
                            : q.substring(0, q.length() - 1));
                }
                break;
            case GLFW.GLFW_KEY_ESCAPE:
                if (HexcessibleState.getShouldPresent())
                    HexcessibleState.setShouldPresent(false);
                else
                    ((GuiSpellcasting) (Object) this).close();
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB:
                // TODO: submit spellcasting query
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        HexcessibleState.setQuery(HexcessibleState.getQuery() + chr);
        return true;
    }
}