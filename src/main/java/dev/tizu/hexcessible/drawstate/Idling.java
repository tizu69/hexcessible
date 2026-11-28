package dev.tizu.hexcessible.drawstate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec2f;

public final class Idling extends DrawState {

    private @Nullable HexPattern hoveredOver;
    private @Nullable PatternEntries.Entry hoveredOverEntry;
    private long hoveredOverStart = 0;
    private Vec2f mousePos = new Vec2f(0, 0);

    public Idling(CastRef castref) {
        super(castref);
    }

    @Override
    public void requestExit() {
        wantsExit = true;
    }

    @Override
    public void onCharType(char chr) {
        if (!Hexcessible.cfg().keyboardDraw.allow)
            return;
        if (Hexcessible.cfg().keyboardDraw.relative && KeyboardDrawing.validSig.contains(chr))
            nextState = new KeyboardDrawing(castref, List.of(Utils.angle(chr)));
        else if (!Hexcessible.cfg().keyboardDraw.relative && KeyboardDrawing.validAbsSig.contains(chr) && chr != 'a')
            nextState = new KeyboardDrawing(castref, List.of(Utils.angle(chr, HexDir.EAST)));
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        var ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (keyCode == GLFW.GLFW_KEY_SPACE && ctrl
                && Hexcessible.cfg().autoComplete.allow) {
            var pos = castref.pxToCoord(mousePos);
            nextState = new AutoCompleting(castref, pos);
        }
        if (keyCode == GLFW.GLFW_KEY_E && ctrl && hoveredOverEntry != null)
            nextState = new AliasChanging(castref, hoveredOverEntry);
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        mousePos = new Vec2f((float) mx, (float) my);
        var hovered = castref.getPatternAt(mx, my);
        if (hovered == null) {
            hoveredOver = null;
            hoveredOverEntry = null;
        } else if (hovered != hoveredOver) {
            hoveredOverStart = System.currentTimeMillis();
            hoveredOver = hovered;
            hoveredOverEntry = PatternEntries.INSTANCE.getFromSig(hovered.getAngles());
        } else if (hoveredOverStart + 500 < System.currentTimeMillis()) {
            KeyboardDrawing.render(ctx, mx, my, hovered.getAngles(), false,
                    Hexcessible.cfg().idle.tooltip, 0);
        }
    }

    /*
     * @Override
     * public void onRender(DrawContext ctx, int mx, int my) {
     * var allDrawMethodsDisabled = !Hexcessible.cfg().keyboardDraw.allow
     * && !Hexcessible.cfg().mouseDraw.allow
     * && !Hexcessible.cfg().autoComplete.allow;
     * var tr = MinecraftClient.getInstance().textRenderer;
     * if (allDrawMethodsDisabled)
     * ctx.drawCenteredTextWithShadow(tr,
     * Text.translatable("hexcessible.no_draw_methods"),
     * ctx.getScaledWindowWidth() / 2, ctx.getScaledWindowHeight() / 2, 16733525);
     * }
     */

    @Override
    public Map<String, String> getHints() {
        var keys = new HashMap<String, String>();

        if (Hexcessible.cfg().keyboardDraw.allow) {
	        var all = Hexcessible.cfg().keyboardDraw.relative
	                ? KeyboardDrawing.validSig : KeyboardDrawing.validAbsSig;
	        var kbdChars = String.join("/", all.subList(0, all.size() / 2)
	                .stream().map(Object::toString).toList());
            keys.put("lmb/" + kbdChars, "draw_start");
        } else {
            keys.put("lmb", "draw_start");
        }

        if (Hexcessible.cfg().autoComplete.allow)
            keys.put("ctrl-space", "auto_complete");
        if (hoveredOverEntry != null)
            keys.put("ctrl-e", "alias");

        return keys;
    }
}
