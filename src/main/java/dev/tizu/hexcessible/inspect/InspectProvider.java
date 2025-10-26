package dev.tizu.hexcessible.inspect;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.tizu.hexcessible.CastingInterfaceAccessor;
import dev.tizu.hexcessible.HexcessibleConfig;
import dev.tizu.hexcessible.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InspectProvider {
    private static final List<Character> validSig = List.of('q', 'w', 'e', 'a', 'd');

    private final CastingInterfaceAccessor castui;
    private long lastMouseMoved = 0;
    private String keyboardSig = "";

    public InspectProvider(CastingInterfaceAccessor castui) {
        this.castui = castui;
    }

    public void onMouseMove() {
        lastMouseMoved = System.currentTimeMillis();
    }

    public boolean onCharType(char chr) {
        if (!castui.isIdle()) // using mouse, ignore
            return false;
        else if (chr == 's') // go back
            removeCharFromSig();
        else if (validSig.contains(chr)) // valid
            keyboardSig += chr;
        else // invalid
            return false;
        return true;
    }

    public boolean onKeyPress(int keyCode, int modifiers) {
        if (keyboardSig.isEmpty() || !castui.isIdle())
            return false;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            removeCharFromSig();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            keyboardSig = "";
            return true;
        }
        return false;
    }

    private void removeCharFromSig() {
        if (keyboardSig.isEmpty())
            return;
        keyboardSig = keyboardSig.substring(0, keyboardSig.length() - 1);
    }

    public void onRender(DrawContext ctx, int mx, int my) {
        renderMouse(ctx, mx, my);
        renderKeyboard(ctx, mx, my);
    }

    private void renderMouse(DrawContext ctx, int mx, int my) {
        if (!HexcessibleConfig.get().inspectMouse
                || System.currentTimeMillis() - lastMouseMoved < 500
                || !castui.isDrawing())
            return;
        var pattern = castui.getPattern().anglesSignature();
        InspectProvider.render(ctx, mx, my, pattern);
    }

    public void renderKeyboard(DrawContext ctx, int mx, int my) {
        if (keyboardSig.isEmpty() || !castui.isIdle())
            return;
        InspectProvider.render(ctx, mx, my, keyboardSig);
    }

    private static void render(DrawContext ctx, int mx, int my, String pattern) {
        var tr = MinecraftClient.getInstance().textRenderer;
        if (pattern.isEmpty())
            return;

        ctx.drawTooltip(tr, Text.literal(pattern), mx, my);

        var entry = PatternEntries.INSTANCE.getFromSig(pattern);
        if (entry == null)
            return;
        var subtext = new ArrayList<Text>();
        subtext.add(Text.literal(entry.toString()).formatted(Formatting.BLUE));
        for (var impl : entry.impls())
            subtext.add(Text.literal(impl.getArgs()).formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, subtext, mx, my + 17);
    }
}
