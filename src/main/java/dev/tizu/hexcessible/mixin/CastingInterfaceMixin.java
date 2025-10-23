package dev.tizu.hexcessible.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.OrderedText;
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

        var tInput = !HexcessibleState.getQuery().equals("")
                ? Text.literal(HexcessibleState.getQuery())
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, tInput, mouseX, mouseY);

        String[] opts = { "foo", "bar", "baz", "qux" };
        var tooltipW = new AtomicInteger(0);
        List<Text> options = IntStream.range(0, opts.length)
                .mapToObj(i -> {
                    var picked = (i == HexcessibleState.getChosen());
                    var prefix = picked ? "> " : "";
                    var fmt = picked ? Formatting.BLUE : Formatting.GRAY;
                    var text = prefix + opts[i] + " " + (i + 1);
                    tooltipW.set(Math.max(tooltipW.get(), tr.getWidth(text)));
                    return Text.literal(text).formatted(fmt);
                })
                .collect(Collectors.toList());

        ctx.drawTooltip(tr, options, mouseX, mouseY + 17);

        var description = "num, num -> num\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam id est scelerisque, congue ex at, ultricies est. Aliquam tortor nibh, bibendum nec lobortis eget, blandit eu dui. Nunc fermentum sapien id felis ullamcorper, non rutrum purus faucibus. Sed fringilla, sapien non bibendum scelerisque, justo ante auctor purus, non elementum sem nulla sed ipsum.";
        List<OrderedText> descLines = tr.wrapLines(Text.literal(description)
                .formatted(Formatting.GRAY), 150);
        var ttp = HoveredTooltipPositioner.INSTANCE;
        ctx.drawTooltip(tr, descLines, ttp, mouseX + tooltipW.get() + 9, mouseY + 17);
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
                    // fall back to normal behavior
                    ((GuiSpellcasting) (Object) this).close();
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB:
                // TODO: accept chosen option
                break;
            case GLFW.GLFW_KEY_UP:
                HexcessibleState.setChosen(HexcessibleState.getChosen() - 1);
                break;
            case GLFW.GLFW_KEY_DOWN:
                HexcessibleState.setChosen(HexcessibleState.getChosen() + 1);
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