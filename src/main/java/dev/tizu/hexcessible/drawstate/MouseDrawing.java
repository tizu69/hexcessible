package dev.tizu.hexcessible.drawstate;

import java.util.List;

import dev.tizu.hexcessible.CastingInterfaceAccessor;
import dev.tizu.hexcessible.CastingInterfaceAccessor.State;
import net.minecraft.client.gui.DrawContext;

public final class MouseDrawing extends DrawState {
    private CastingInterfaceAccessor castui;
    private long lastMouseMoved = 0;

    public MouseDrawing(CastCalc calc, CastingInterfaceAccessor castui) {
        super(calc);
        this.castui = castui;
    }

    @Override
    public void onMouseMove(double mx, double my) {
        lastMouseMoved = System.currentTimeMillis();
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (lastMouseMoved + 500 > System.currentTimeMillis() ||
                castui.getState() != State.DRAWING)
            return;
        var sig = castui.getPattern().anglesSignature();
        KeyboardDrawing.render(ctx, mx, my, sig, "");
    }

    @Override
    public List<String> getDebugInfo() {
        var moveMs = (System.currentTimeMillis() - lastMouseMoved);
        var moveDebug = "Last move: " + (moveMs > 2000 ? ">2000ms" : moveMs + "ms");
        return List.of(moveDebug);
    }
}