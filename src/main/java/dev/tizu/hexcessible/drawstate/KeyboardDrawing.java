package dev.tizu.hexcessible.drawstate;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class KeyboardDrawing extends DrawState {
    public static final List<Character> validSig = List.of('q', 'w', 'e', 'a', 'd');

    private String sig;
    private HexCoord start = new HexCoord(0, 0);

    public KeyboardDrawing(CastCalc calc, String sig) {
        super(calc);
        this.sig = sig;
    }

    public KeyboardDrawing(CastCalc calc, String sig, HexCoord start) {
        super(calc);
        this.sig = sig;
        this.start = start;
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (sig.isEmpty())
            requestExit();
        KeyboardDrawing.render(ctx, mx, my, sig, "␣⇥↩");
    }

    @Override
    public void onCharType(char chr) {
        if (chr == 's') // go back
            removeCharFromSig();
        else if (validSig.contains(chr)) // valid
            sig += chr;
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
            removeCharFromSig();
    }

    private void removeCharFromSig() {
        if (sig.isEmpty())
            return;
        sig = sig.substring(0, sig.length() - 1);
    }

    public static void render(DrawContext ctx, int mx, int my, String pattern,
            String submitKeys) {
        var tr = MinecraftClient.getInstance().textRenderer;
        if (pattern.isEmpty())
            return;

        var text = Text.literal(pattern);
        if (!submitKeys.isEmpty())
            text = text.append(Text.literal(" " + submitKeys)
                    .formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, text, mx, my);

        var entry = PatternEntries.INSTANCE.getFromSig(pattern);
        if (entry == null)
            return;
        var subtext = new ArrayList<Text>();
        subtext.add(Text.literal(entry.toString()).formatted(Formatting.BLUE));
        for (var impl : entry.impls())
            subtext.add(Text.literal(impl.getArgs()).formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, subtext, mx, my + 17);
    }

    @Override
    public boolean allowStartDrawing() {
        return sig.isEmpty();
    }

    @Override
    public void onMousePress(double mx, double my, int button) {
        if (button == 0)
            requestExit();
    }
}