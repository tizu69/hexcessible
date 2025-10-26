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

    protected CastCalc calc;
    protected DrawState nextState = null;
    protected boolean wantsExit = false;

    public DrawState(CastCalc calc) {
        this.calc = calc;
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
        nextState = getNew(this.calc);
    }

    public List<String> getDebugInfo() {
        return List.of();
    }

    public boolean allowStartDrawing() {
        return true;
    }

    public static DrawState getNew(CastCalc calc) {
        return new Idling(calc);
    }

    public static DrawState getNew(GuiSpellcasting ui) {
        return getNew(new CastCalc(ui));
    }

    @Nullable
    public static DrawState updateRequired(GuiSpellcasting ui, DrawState current) {
        if (current.nextState != null)
            return current.nextState;
        var castui = new CastingInterfaceAccessor(ui);
        var hexState = castui.getState();
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
        var calc = new CastCalc(ui);
        return switch (hexState) {
            case BETWEENPATTERNS -> new Idling(calc);
            case JUSTSTARTED -> new AutoCompleting(calc, castui.getStart());
            case DRAWING -> new MouseDrawing(calc, castui);
        };
    }

    public static boolean shouldClose(DrawState current) {
        return current.wantsExit;
    }

    public static class CastCalc {
        private final GuiSpellcasting castui;

        private CastCalc(GuiSpellcasting castui) {
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
