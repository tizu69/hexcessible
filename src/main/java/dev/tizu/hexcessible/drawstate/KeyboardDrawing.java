package dev.tizu.hexcessible.drawstate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

public final class KeyboardDrawing extends DrawState {
    public static final List<Character> validSig = List.of(
            'q', 'w', 'e', 'a', 'd', 'Q', 'W', 'E', 'A', 'D');
    public static final List<Character> validAbsSig = List.of(
            'q', 'e', 'a', 'd', 'z', 'c', 'Q', 'E', 'A', 'D', 'Z', 'C');
    public static final int COLOR1 = 0xff_64c8ff;
    public static final int COLOR2 = 0xff_fecbe6;
    public static final int COLOR3 = 0xaa_363a4f;
    public static final int COLOR4 = 0xaa_6e738d;

    private List<HexAngle> sig;
    private HexCoord origin;
    private HexDir originDir = HexDir.EAST;
    private @Nullable HexCoord start;
    private @Nullable HexDir startDir;
    private @Nullable HexCoord end;
    private @Nullable HexDir endDir;
    private KeyboardDrawing nextDrawing;

    public KeyboardDrawing(CastRef castref, List<HexAngle> sig) {
        super(castref);
        this.sig = new ArrayList<>(sig);
        this.origin = new HexCoord(0, 0);
        recalculateNewAll();
    }

    public KeyboardDrawing(CastRef castref, HexCoord start, List<List<HexAngle>> sigs, HexDir dir) {
        super(castref);
        if (sigs.isEmpty())
            throw new IllegalArgumentException();
        this.sig = new ArrayList<>(sigs.get(0));
        this.origin = start;
        if (sigs.size() > 1)
            this.nextDrawing = new KeyboardDrawing(castref, start,
                    sigs.subList(1, sigs.size()), dir);
        this.originDir = dir;
        recalculateNewAll();
    }

    @Override
    public void requestExit() {
        if (nextDrawing != null) {
            // we may have placed a thingy where it wasn't when nextDrawing got
            // initialized, so we recalculate it (this fixes chaining overlap)
            nextDrawing.recalculateNewAll();
            nextState = nextDrawing;
        } else
            super.requestExit();
    }

    private int queuedCount() {
        if (nextDrawing == null)
            return 0;
        return 1 + nextDrawing.queuedCount();
    }

    public void recalculateNewAll() {
        if (sig.isEmpty()) {
            start = origin;
            startDir = originDir;
            end = origin;
            endDir = originDir;
            return;
        }

        var mutated = castref.findClosestAvailable(origin,
                new HexPattern(originDir, sig));
        if (mutated == null) {
            start = null;
            startDir = null;
            end = null;
            endDir = null;
            return;
        }
        start = mutated.coord();
        startDir = mutated.startDir();

        var pat = new HexPattern(startDir, sig);
        end = Utils.finalPos(start, pat);
        endDir = pat.finalDir();
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (sig.isEmpty())
            requestExit();
        renderPattern(ctx);
        if (Hexcessible.cfg().keyboardDraw.keyHint)
            renderNextPointTooltips(ctx);
        var pos = castref.coordToPx(end == null ? origin : end);
        var x = pos.x + 20;
        KeyboardDrawing.render(ctx, (int) x, (int) pos.y, sig, start == null,
                Hexcessible.cfg().keyboardDraw.tooltip, queuedCount());
    }

    @Override
    public void onCharType(char chr) {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            return;
        if (Character.toLowerCase(chr) == 's') { // go back
            removeCharFromSig();
        } else if (Hexcessible.cfg().keyboardDraw.relative && validSig.contains(chr)) { // valid
            var angle = Utils.angle(chr);
            if (canGo(angle)) {
                sig.add(angle);
                recalculateNewAll();
            }
        } else if (!Hexcessible.cfg().keyboardDraw.relative && validAbsSig.contains(chr)) { // valid
            var angle = Utils.angle(chr, endDir);
            if (canGo(angle)) {
                sig.add(angle);
                recalculateNewAll();
            }
        }
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        var shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
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
            case GLFW.GLFW_KEY_R:
                rotate(shift ? -1 : 1);
                break;
            default:
        }
    }

    private void submit() {
        recalculateNewAll();
        if (start == null)
            return;
        castref.execute(new HexPattern(startDir, sig), start);
        requestExit();
    }

    private void moveOrigin(int x, int y) {
        var next = origin.plus(new HexCoord(x, y));
        if (castref.isVisible(next)) // don't allow out of bounds
            origin = next;
        recalculateNewAll();
    }

    private void rotate(int delta) {
        var i = Math.floorMod(originDir.ordinal() + delta, HexDir.values().length);
        originDir = HexDir.values()[i];
        recalculateNewAll();
    }

    @Override
    public boolean onMouseScroll(int delta) {
        rotate(-delta);
        return true;
    }

    private void removeCharFromSig() {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            requestExit();
        if (sig.isEmpty())
            return;
        sig.remove(sig.size() - 1);
        recalculateNewAll();
    }

    public void renderPattern(DrawContext ctx) {
        if (Hexcessible.cfg().keyboardDraw.ghost)
            renderPattern(ctx, origin, originDir, sig, COLOR3, COLOR4);
        if (start != null)
            renderPattern(ctx, start, startDir, sig, COLOR1, COLOR2);

        if (!Hexcessible.cfg().debug)
            return;
        if (start != null)
            drawLine(ctx, origin, start);
        var mat = ctx.getMatrices().peek().getPositionMatrix();
        if (start != null)
            RenderLib.drawSpot(mat, castref.coordToPx(start), 6f, 0f, 0f, 1f, 1f);
        RenderLib.drawSpot(mat, castref.coordToPx(origin), 6f, 0f, 1f, 0f, 1f);
    }

    public void renderPattern(DrawContext ctx, HexCoord start, HexDir startDir,
            List<HexAngle> sig, int color1, int color2) {
        if (start == null || startDir == null)
            return;
        var mat = ctx.getMatrices().peek().getPositionMatrix();
        var pat = new HexPattern(startDir, sig);
        var duplicates = RenderLib.findDupIndices(pat.positions());

        var points = new ArrayList<Vec2f>();
        for (var c : pat.positions())
            points.add(castref.coordToPx(new HexCoord(
                    c.getQ() + start.getQ(),
                    c.getR() + start.getR())));

        RenderLib.drawPatternFromPoints(mat, points, duplicates, false, color1,
                color2, 0.1f, RenderLib.DEFAULT_READABILITY_OFFSET, 1f, 0);
    }

    private void renderNextPointTooltips(DrawContext ctx) {
        if (end == null || endDir == null)
            return;
        var tr = MinecraftClient.getInstance().textRenderer;
        var endpx = castref.coordToPx(end);
        for (var angle : HexAngle.values()) {
            var pos = end.plus(endDir.rotatedBy(angle));
            var charstr = Hexcessible.cfg().keyboardDraw.relative
                    ? Utils.angle(angle)
                    : String.valueOf(Utils.absAngle(endDir.rotatedBy(angle)));
            if (castref.isUsed(pos) || !canGo(angle) || charstr == null)
                continue;
            var px = castref.coordToPx(pos);
            var dx = px.x - endpx.x;
            var dy = px.y - endpx.y;
            var distance = Math.sqrt(dx * dx + dy * dy);
            var targetX = endpx.x + (dx / distance) * 20;
            var targetY = endpx.y + (dy / distance) * 20;
            ctx.drawCenteredTextWithShadow(tr, Text.literal(charstr),
                    (int) targetX - 1, (int) targetY - 5, 0xff_A8A8A8);
        }
    }

    private boolean canGo(@Nullable HexAngle angle) {
        if (angle == null || startDir == null)
            return false;
        var pat = new HexPattern(this.startDir, new ArrayList<>(sig));
        return castref.isValidPatternAddition(pat, angle);
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
            boolean failed, HexcessibleConfig.Tooltip tooltip, int queued) {
        var tr = MinecraftClient.getInstance().textRenderer;
        if (sig.isEmpty() || !tooltip.visible()) {
            if (failed)
                ctx.drawTooltip(tr, Text.translatable("hexcessible.no_space")
                        .formatted(Formatting.RED), mx, y);
            return;
        }

        var text = Text.literal(Utils.angle(sig, Hexcessible.cfg().uppercaseSig));
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
        recalculateNewAll();
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

    @Override
    public Map<String, String> getHints() {
        var keys = new HashMap<String, String>();

        if (Hexcessible.cfg().keyboardDraw.allow) {
        	var all = Hexcessible.cfg().keyboardDraw.relative
         			? KeyboardDrawing.validSig : KeyboardDrawing.validAbsSig;
            var kbdChars = String.join("/", all.subList(0, all.size() / 2)
            		.stream().map(Object::toString).toList());
            keys.put(kbdChars, "draw_start");
            keys.put("bksp/s", "undo");
        }

        keys.put("lmb/tab/enter/space", "cast");
        keys.put("drag/h/j/k/l/←/↓/↑/→", "move");
        keys.put("wheel/r/shift-r", "rotate");

        return keys;
    }
}
