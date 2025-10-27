package dev.tizu.hexcessible.drawstate;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.RenderLib;
import dev.tizu.hexcessible.accessor.CastRef;
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
    private HexCoord origin = new HexCoord(0, 0);
    private HexDir dir = HexDir.EAST;

    public KeyboardDrawing(CastRef castref, String sig) {
        super(castref);
        this.sig = sig;
    }

    public KeyboardDrawing(CastRef castref, String sig, HexCoord start) {
        super(castref);
        this.sig = sig;
        this.origin = start;
    }

    @Nullable
    private HexCoord start() {
        if (sig.isEmpty())
            return origin;
        var mutated = castref.findClosestAvailable(origin,
                new HexPattern(HexDir.EAST, getAngles()));
        if (mutated == null)
            return null;
        dir = mutated.startDir();
        return mutated.coord();
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
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                removeCharFromSig();
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_SPACE:
                var start = start();
                if (start == null)
                    return;
                castref.execute(new HexPattern(dir, getAngles()), start);
                requestExit();
                break;
            case GLFW.GLFW_KEY_H, GLFW.GLFW_KEY_LEFT:
                origin = origin.plus(new HexCoord(-1, 0));
                break;
            case GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_DOWN:
                origin = origin.plus(new HexCoord(0, 1));
                break;
            case GLFW.GLFW_KEY_K, GLFW.GLFW_KEY_UP:
                origin = origin.plus(new HexCoord(0, -1));
                break;
            case GLFW.GLFW_KEY_L, GLFW.GLFW_KEY_RIGHT:
                origin = origin.plus(new HexCoord(1, 0));
                break;
            default:
        }
    }

    private void removeCharFromSig() {
        if (sig.isEmpty())
            return;
        sig = sig.substring(0, sig.length() - 1);
    }

    public void renderPattern(DrawContext ctx) {
        var mat = ctx.getMatrices().peek().getPositionMatrix();
        var pat = new HexPattern(dir, getAngles());
        var duplicates = RenderLib.findDupIndices(pat.positions());

        var start = start();
        if (start == null)
            return;

        var points = new ArrayList<Vec2f>();
        for (var c : pat.positions())
            points.add(castref.coordToPx(new HexCoord(
                    c.getQ() + start.getQ(),
                    c.getR() + start.getR())));

        RenderLib.drawPatternFromPoints(mat, points, duplicates, false, COLOR1,
                COLOR2, 0.1f, RenderLib.DEFAULT_READABILITY_OFFSET, 1f, 0);

        drawLine(ctx, origin, start);
        RenderLib.drawSpot(mat, castref.coordToPx(origin), 6f, 1f, 0f, 0f, 1f);
        RenderLib.drawSpot(mat, castref.coordToPx(start), 6f, 0f, 0f, 1f, 1f);
    }

    private void drawLine(DrawContext ctx, HexCoord start, HexCoord end) {
        var startpx = castref.coordToPx(start);
        var endpx = castref.coordToPx(end);
        var dx = endpx.x - startpx.x;
        var dy = endpx.y - startpx.y;
        var length = Math.sqrt(dx * dx + dy * dy);
        var steps = (int) Math.ceil(length / 2);
        for (var i = 0; i < steps; i++) {
            var x = startpx.x + dx * i / steps;
            var y = startpx.y + dy * i / steps;
            ctx.fill((int) x, (int) y, (int) x + 2, (int) y + 2, COLOR2);
        }
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