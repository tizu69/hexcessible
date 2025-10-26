package dev.tizu.hexcessible.drawstate;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.RenderLib;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

public final class KeyboardDrawing extends DrawState {
    public static final List<Character> validSig = List.of('q', 'w', 'e', 'a', 'd');
    public static final int COLOR1 = 0xff_64c8ff;
    public static final int COLOR2 = 0xff_fecbe6;

    private String sig;
    private HexCoord start = new HexCoord(0, 0);

    public KeyboardDrawing(CastRef castref, String sig) {
        super(castref);
        this.sig = sig;
    }

    public KeyboardDrawing(CastRef castref, String sig, HexCoord start) {
        super(castref);
        this.sig = sig;
        this.start = start;
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (sig.isEmpty())
            requestExit();
        renderPattern(ctx);
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

    public void renderPattern(DrawContext ctx) {
        var mat = ctx.getMatrices().peek().getPositionMatrix();
        var pat = new HexPattern(HexDir.EAST, getAngles());
        var duplicates = RenderLib.findDupIndices(pat.positions());

        var points = new ArrayList<Vec2f>();
        for (var c : pat.positions())
            points.add(castref.coordToPx(new HexCoord(
                    c.getQ() + start.getQ(),
                    c.getR() + start.getR())));

        RenderLib.drawPatternFromPoints(mat, points, duplicates, false, COLOR1,
                COLOR2, 0.1f, RenderLib.DEFAULT_READABILITY_OFFSET, 1f, 0);
    }

    public List<HexAngle> getAngles() {
        var angles = new ArrayList<HexAngle>();
        for (var c : sig.chars().toArray()) {
            angles.add(switch (c) {
                case 'q' -> HexAngle.LEFT;
                case 'w' -> HexAngle.FORWARD;
                case 'e' -> HexAngle.RIGHT;
                case 'a' -> HexAngle.LEFT_BACK;
                case 'd' -> HexAngle.RIGHT_BACK;
                default -> throw new IllegalStateException(c + " invalid");
            });
        }
        return angles;
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