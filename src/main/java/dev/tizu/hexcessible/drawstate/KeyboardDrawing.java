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
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.HexcessibleConfig;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.entries.PatternEntries;
import kotlin.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

public final class KeyboardDrawing extends DrawState {
    public static final List<Character> validSig = List.of(
            'q', 'w', 'e', 'a', 'd', 'Q', 'W', 'E', 'A', 'D');
    public static final int COLOR1 = 0xff_64c8ff;
    public static final int COLOR2 = 0xff_fecbe6;

    private List<HexAngle> sig;
    private HexCoord origin;
    private HexDir startDir = HexDir.EAST;
    private KeyboardDrawing nextDrawing;

    public KeyboardDrawing(CastRef castref, List<HexAngle> sig) {
        this(castref, sig, new HexCoord(0, 0));
    }

    public KeyboardDrawing(CastRef castref, List<HexAngle> sig, HexCoord start) {
        super(castref);
        this.sig = new ArrayList<>(sig);
        this.origin = start;
    }

    public KeyboardDrawing(CastRef castref, HexCoord start, List<List<HexAngle>> sigs) {
        super(castref);
        if (sigs.isEmpty())
            throw new IllegalArgumentException();
        this.sig = new ArrayList<>(sigs.get(0));
        this.origin = start;
        if (sigs.size() > 1)
            this.nextDrawing = new KeyboardDrawing(castref, start,
                    sigs.subList(1, sigs.size()));
    }

    @Override
    public void requestExit() {
        if (nextDrawing != null)
            nextState = nextDrawing;
        else
            super.requestExit();
    }

    private int queuedCount() {
        if (nextDrawing == null)
            return 0;
        return 1 + nextDrawing.queuedCount();
    }

    @Nullable
    private HexCoord start() {
        if (sig.isEmpty())
            return origin;
        var mutated = castref.findClosestAvailable(origin,
                new HexPattern(HexDir.EAST, sig));
        if (mutated == null)
            return null;
        startDir = mutated.startDir();
        return mutated.coord();
    }

    private @Nullable Pair<HexCoord, HexDir> end() {
        var start = start();
        if (start == null)
            return null;
        var pat = new HexPattern(startDir, sig);
        return new Pair<>(Utils.finalPos(start, pat), pat.finalDir());
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (sig.isEmpty())
            requestExit();
        renderPattern(ctx);
        if (Hexcessible.cfg().keyboardDraw.keyHint)
            renderNextPointTooltips(ctx);
        KeyboardDrawing.render(ctx, mx, my, sig, "␣⇥↩", start() == null,
                Hexcessible.cfg().keyboardDraw.tooltip, queuedCount());
    }

    @Override
    public void onCharType(char chr) {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            return;
        if (Character.toLowerCase(chr) == 's') // go back
            removeCharFromSig();
        else if (validSig.contains(chr)
                && canGo(Utils.angle(chr))) // valid
            sig.add(Utils.angle(chr));
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                removeCharFromSig();
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB, GLFW.GLFW_KEY_SPACE:
                submit();
                break;
            case GLFW.GLFW_KEY_H, GLFW.GLFW_KEY_LEFT:
                moveOrigin(-1, 0);
                break;
            case GLFW.GLFW_KEY_J, GLFW.GLFW_KEY_DOWN:
                moveOrigin(0, 1);
                break;
            case GLFW.GLFW_KEY_K, GLFW.GLFW_KEY_UP:
                moveOrigin(0, -1);
                break;
            case GLFW.GLFW_KEY_L, GLFW.GLFW_KEY_RIGHT:
                moveOrigin(1, 0);
                break;
            default:
        }
    }

    private void submit() {
        var start = start();
        if (start == null)
            return;
        castref.execute(new HexPattern(startDir, sig), start);
        requestExit();
    }

    private void moveOrigin(int x, int y) {
        var next = origin.plus(new HexCoord(x, y));
        if (castref.isVisible(next)) // don't allow out of bounds
            origin = next;
    }

    private void removeCharFromSig() {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            requestExit();
        if (sig.isEmpty())
            return;
        sig.remove(sig.size() - 1);
    }

    public void renderPattern(DrawContext ctx) {
        var mat = ctx.getMatrices().peek().getPositionMatrix();
        var pat = new HexPattern(startDir, sig);
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

        if (!Hexcessible.cfg().debug)
            return;
        drawLine(ctx, origin, start);
        RenderLib.drawSpot(mat, castref.coordToPx(start), 6f, 0f, 0f, 1f, 1f);
        RenderLib.drawSpot(mat, castref.coordToPx(origin), 6f, 0f, 1f, 0f, 1f);
    }

    private void renderNextPointTooltips(DrawContext ctx) {
        var end = end();
        if (end == null)
            return;
        var point = end.getFirst();
        var dir = end.getSecond();

        var tr = MinecraftClient.getInstance().textRenderer;
        for (var angle : HexAngle.values()) {
            var pos = point.plus(dir.rotatedBy(angle));
            var charstr = angleAsCharStr(angle);
            if (castref.isUsed(pos) || !canGo(angle) || charstr == null)
                continue;
            var px = castref.coordToPx(pos);
            ctx.drawCenteredTextWithShadow(tr, Text.literal(charstr),
                    (int) px.x + 1, (int) px.y - 10, 0xff_A8A8A8);
        }
    }

    private boolean canGo(HexAngle angle) {
        var pat = new HexPattern(this.startDir, new ArrayList<>(sig));
        return castref.isValidPatternAddition(pat, angle);
    }

    private static @Nullable String angleAsCharStr(HexAngle angle) {
        return switch (angle) {
            case LEFT -> "Q";
            case FORWARD -> "W";
            case RIGHT -> "E";
            case LEFT_BACK -> "A";
            case RIGHT_BACK -> "D";
            default -> null;
        };
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

    public static void render(DrawContext ctx, int mx, int y, List<HexAngle> sig,
            String submitKeys, boolean failed, HexcessibleConfig.Tooltip tooltip,
            int queued) {
        var tr = MinecraftClient.getInstance().textRenderer;
        if (sig.isEmpty() || !tooltip.visible())
            return;

        var text = Text.literal(Utils.angle(sig, Hexcessible.cfg().uppercaseSig));
        if (!submitKeys.isEmpty() && !failed)
            text = text.append(Text.literal(" " + submitKeys)
                    .formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, text, mx, y);
        y += 17;

        if (failed) {
            ctx.drawTooltip(tr, Text.translatable("hexcessible.no_space")
                    .formatted(Formatting.RED), mx, y);
            y += 17;
        }

        if (queued > 0) {
            ctx.drawTooltip(tr, Text.translatable("hexcessible.count_queued",
                    queued).formatted(Formatting.YELLOW), mx, y);
            y += 17;
        }

        var entry = PatternEntries.INSTANCE.getFromSig(sig);
        if (entry == null || !tooltip.descriptive())
            return;
        var subtext = new ArrayList<Text>();
        subtext.add(Text.literal(entry.toString()).formatted(Formatting.BLUE));
        for (var impl : entry.impls())
            subtext.add(Text.literal(impl.getArgs()).formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, subtext, mx, y);
    }

    @Override
    public void onMouseMove(double mx, double my) {
        origin = castref.pxToCoord(new Vec2f((int) mx, (int) my));
    }

    @Override
    public boolean allowStartDrawing() {
        return sig.isEmpty();
    }

    @Override
    public void onMousePress(double mx, double my, int button) {
        if (button == 1)
            requestExit();
        if (button == 0)
            submit();
    }
}