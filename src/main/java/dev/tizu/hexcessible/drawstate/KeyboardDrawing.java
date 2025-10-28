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
    public static final List<Character> validSig = List.of('q', 'w', 'e', 'a', 'd');
    public static final int COLOR1 = 0xff_64c8ff;
    public static final int COLOR2 = 0xff_fecbe6;

    private HexPattern pattern;
    private HexCoord origin = new HexCoord(0, 0);
    private @Nullable CastRef.PatternPlacement placement;

    public KeyboardDrawing(CastRef castref, String sig) {
        super(castref);
        this.pattern = HexPattern.fromAngles(sig, HexDir.EAST);
        updatePlacement();
    }

    public KeyboardDrawing(CastRef castref, String sig, HexCoord start) {
        super(castref);
        this.pattern = HexPattern.fromAngles(sig, HexDir.EAST);
        this.origin = start;
        updatePlacement();
    }

    private void updatePlacement() {
        if (pattern.getAngles().isEmpty())
            this.placement = new CastRef.PatternPlacement(origin, pattern.getStartDir());
        else
            this.placement = castref.findClosestAvailable(origin, pattern);
    }

    private @Nullable HexCoord getEndPos() {
        if (placement == null)
            return null;
        var dir = placement.startDir();
        var pos = placement.coord();
        for (var angle : pattern.getAngles()) {
            dir = dir.rotatedBy(angle);
            pos = pos.plus(dir);
        }
        return pos;
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (pattern.getAngles().isEmpty())
            requestExit();
        renderPattern(ctx);
        if (Hexcessible.cfg().keyboardDraw.keyHint)
            renderNextPointTooltips(ctx);
        KeyboardDrawing.render(ctx, mx, my, pattern.anglesSignature(), "␣⇥↩",
                placement == null, Hexcessible.cfg().keyboardDraw.tooltip);
    }

    @Override
    public void onCharType(char chr) {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            return;
        if (chr == 's') // go back
            removeAngleFromPattern();
        else if (validSig.contains(chr)) // valid
            addAngleToPattern(charToAngle(chr));

    }

    private void addAngleToPattern(HexAngle angle) {
        pattern.tryAppendDir(pattern.finalDir().rotatedBy(angle));
        updatePlacement();
    }

    private HexAngle charToAngle(char c) {
        return switch (c) {
            case 'q' -> HexAngle.LEFT;
            case 'w' -> HexAngle.FORWARD;
            case 'e' -> HexAngle.RIGHT;
            case 'a' -> HexAngle.LEFT_BACK;
            case 'd' -> HexAngle.RIGHT_BACK;
            default -> throw new IllegalStateException(c + " invalid");
        };
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                removeAngleFromPattern();
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
        if (placement == null)
            return;
        castref.execute(pattern, placement.coord());
        requestExit();
    }

    private void moveOrigin(int x, int y) {
        var next = origin.plus(new HexCoord(x, y));
        if (castref.isVisible(next)) // don't allow out of bounds
            origin = next;
        updatePlacement();
    }

    private void removeAngleFromPattern() {
        if (!Hexcessible.cfg().keyboardDraw.allow) {
            requestExit();
            return;
        }
        var angles = pattern.getAngles();
        if (angles.isEmpty())
            return;
        var newAngles = new ArrayList<>(angles.subList(0, angles.size() - 1));
        pattern = new HexPattern(pattern.getStartDir(), newAngles);
        updatePlacement();
    }

    public void renderPattern(DrawContext ctx) {
        if (placement == null)
            return;

        var mat = ctx.getMatrices().peek().getPositionMatrix();
        var pat = new HexPattern(placement.startDir(), pattern.getAngles());
        var duplicates = RenderLib.findDupIndices(pat.positions());

        var startCoord = placement.coord();
        var points = new ArrayList<Vec2f>();
        for (var c : pat.positions())
            points.add(castref.coordToPx(new HexCoord(
                    c.getQ() + startCoord.getQ(),
                    c.getR() + startCoord.getR())));

        RenderLib.drawPatternFromPoints(mat, points, duplicates, false, COLOR1,
                COLOR2, 0.1f, RenderLib.DEFAULT_READABILITY_OFFSET, 1f, 0);

        if (!Hexcessible.cfg().debug)
            return;
        drawLine(ctx, origin, startCoord);
        RenderLib.drawSpot(mat, castref.coordToPx(startCoord), 6f, 0f, 0f, 1f, 1f);
        RenderLib.drawSpot(mat, castref.coordToPx(origin), 6f, 0f, 1f, 0f, 1f);
    }

    private void renderNextPointTooltips(DrawContext ctx) {
        if (placement == null)
            return;

        var tr = MinecraftClient.getInstance().textRenderer;
        var center = Utils.finalPos(placement.coord(), pattern);
        for (var angle : HexAngle.values()) {
            var pos = center.plus(pattern.finalDir().rotatedBy(angle));
            var charstr = angleAsCharStr(angle);
            if (castref.isUsed(pos)
                    || !castref.isValidPatternAddition(pattern, angle)
                    || charstr == null)
                continue;
            var px = castref.coordToPx(pos);
            ctx.drawCenteredTextWithShadow(tr, Text.literal(charstr),
                    (int) px.x + 1, (int) px.y - 5, 0xff_A8A8A8);
        }
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

    public static void render(DrawContext ctx, int mx, int my, String pattern,
            String submitKeys, boolean failed, HexcessibleConfig.Tooltip tooltip) {
        var y = my;
        var tr = MinecraftClient.getInstance().textRenderer;
        if (pattern.isEmpty() || !tooltip.visible())
            return;

        var text = Text.literal(pattern);
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

        var entry = PatternEntries.INSTANCE.getFromSig(pattern);
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
        updatePlacement();
    }

    @Override
    public boolean allowStartDrawing() {
        return pattern.getAngles().isEmpty();
    }

    @Override
    public void onMousePress(double mx, double my, int button) {
        if (button == 1)
            requestExit();
        if (button == 0)
            submit();
    }
}
