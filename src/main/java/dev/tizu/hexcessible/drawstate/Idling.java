package dev.tizu.hexcessible.drawstate;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.accessor.CastRef;

public final class Idling extends DrawState {
    public Idling(CastRef castref) {
        super(castref);
    }

    @Override
    public void requestExit() {
        wantsExit = true;
    }

    @Override
    public void onCharType(char chr) {
        if (Hexcessible.cfg().keyboardDraw.allow
                && KeyboardDrawing.validSig.contains(chr))
            nextState = new KeyboardDrawing(castref, List.of(Utils.angle(chr)));
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        var ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (keyCode == GLFW.GLFW_KEY_SPACE && ctrl
                && Hexcessible.cfg().autoComplete.allow)
            nextState = new AutoCompleting(castref);
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
}
