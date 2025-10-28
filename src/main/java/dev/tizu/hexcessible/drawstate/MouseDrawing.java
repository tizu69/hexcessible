package dev.tizu.hexcessible.drawstate;

import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor.State;
import net.minecraft.client.gui.DrawContext;

public final class MouseDrawing extends DrawState {
    private CastingInterfaceAccessor accessor;

    public MouseDrawing(CastRef castref, CastingInterfaceAccessor accessor) {
        super(castref);
        this.accessor = accessor;
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (accessor.getState() != State.DRAWING)
            return;
        var sig = accessor.getPattern().getAngles();
        KeyboardDrawing.render(ctx, mx, my, sig, "", false,
                Hexcessible.cfg().mouseDraw.tooltip);
    }
}