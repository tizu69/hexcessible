package dev.tizu.hexcessible.drawstate;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.accessor.CastRef;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public sealed class DrawState
        permits Idling, MouseDrawing, KeyboardDrawing, AutoCompleting, AliasChanging {

    protected CastRef castref;
    protected DrawState nextState = null;
    protected boolean wantsExit = false;

    public DrawState(CastRef castref) {
        this.castref = castref;
    }

    public void onRender(GuiGraphics ctx, int mx, int my) {
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

    public boolean onMouseScroll(int delta) {
        // no-op
        return false;
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

    /**
     * Modifiers: ctrl, shift
     * Combined press: ctrl-a
     * Alternatives: q/w/e/a/d
     * Mouse: lmb, rmb, drag, wheel
     */
    public Map<String, String> getHints() {
        return Map.of();
    }

    public static DrawState getNew(CastRef castref) {
        return new Idling(castref);
    }

    @Nullable
    public static DrawState updateRequired(GuiSpellcasting castui, DrawState current) {
        if (current.nextState != null)
            return current.nextState;
        var hexState = current.castref.internals().getState();
        var allowed = switch (hexState) {
            case BETWEENPATTERNS ->
                List.of(Idling.class,
                        KeyboardDrawing.class,
                        AliasChanging.class,
                        AutoCompleting.class); // mouse released while autocompleting
            case JUSTSTARTED ->
                List.of(MouseDrawing.class, // started -> drawing -> undone
                        AliasChanging.class,
                        AutoCompleting.class);
            case DRAWING ->
                List.of(MouseDrawing.class);
        };
        if (allowed.contains(current.getClass()))
            return null;
        return switch (hexState) {
            case BETWEENPATTERNS -> new Idling(current.castref);
            case JUSTSTARTED -> new AutoCompleting(current.castref,
                    current.castref.internals().getStart());
            case DRAWING -> new MouseDrawing(current.castref);
        };
    }

    public static boolean shouldClose(DrawState current) {
        return current.wantsExit;
    }
}
