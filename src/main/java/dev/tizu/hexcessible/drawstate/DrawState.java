package dev.tizu.hexcessible.drawstate;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.CastingInterfaceAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec2f;

public sealed class DrawState
        permits Idling, MouseDrawing, KeyboardDrawing, AutoCompleting {

    protected CastRef castref;
    protected DrawState nextState = null;
    protected boolean wantsExit = false;

    public DrawState(CastRef castref) {
        this.castref = castref;
    }

    public void onRender(DrawContext ctx, int mx, int my) {
        // no-op
    }

    public void onCharType(char chr) {
        // no-op
    }

    public void onKeyPress(int keyCode, int modifiers) {
        // no-op
    }

    public void onMouseMove(double mx, double my) {
        // no-op
    }

    public void onMousePress(double mx, double my, int button) {
        // no-op
    }

    public void requestExit() {
        nextState = getNew(this.castref);
    }

    public List<String> getDebugInfo() {
        return List.of();
    }

    public boolean allowStartDrawing() {
        return true;
    }

    public static DrawState getNew(CastRef castref) {
        return new Idling(castref);
    }

    public static DrawState getNew(GuiSpellcasting castui) {
        return getNew(new CastRef(castui));
    }

    @Nullable
    public static DrawState updateRequired(GuiSpellcasting castui, DrawState current) {
        if (current.nextState != null)
            return current.nextState;
        var accessor = new CastingInterfaceAccessor(castui);
        var hexState = accessor.getState();
        var allowed = switch (hexState) {
            case BETWEENPATTERNS ->
                List.of(Idling.class,
                        KeyboardDrawing.class,
                        AutoCompleting.class); // mouse released while autocompleting
            case JUSTSTARTED ->
                List.of(MouseDrawing.class, // started -> drawing -> undone
                        AutoCompleting.class);
            case DRAWING ->
                List.of(MouseDrawing.class);
        };
        if (allowed.contains(current.getClass()))
            return null;
        var castref = new CastRef(castui);
        return switch (hexState) {
            case BETWEENPATTERNS -> new Idling(castref);
            case JUSTSTARTED -> new AutoCompleting(castref, accessor.getStart());
            case DRAWING -> new MouseDrawing(castref, accessor);
        };
    }

    public static boolean shouldClose(DrawState current) {
        return current.wantsExit;
    }

    public static class CastRef {
        private final GuiSpellcasting castui;

        private CastRef(GuiSpellcasting castui) {
            this.castui = castui;
        }

        public HexCoord pxToCoord(Vec2f px) {
            return castui.pxToCoord(px);
        }

        public Vec2f coordToPx(HexCoord coord) {
            return castui.coordToPx(coord);
        }

        public float hexSize() {
            return castui.hexSize();
        }
    }
}
