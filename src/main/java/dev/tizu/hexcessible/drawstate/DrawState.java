package dev.tizu.hexcessible.drawstate;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.CastingInterfaceAccessor;
import net.minecraft.client.gui.DrawContext;

public sealed class DrawState
        permits Idling, MouseDrawing, KeyboardDrawing, AutoCompleting {

    protected DrawState nextState = null;
    protected boolean wantsExit = false;

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
        nextState = getNew();
    }

    public List<String> getDebugInfo() {
        return List.of();
    }

    public boolean allowStartDrawing() {
        return true;
    }

    public static DrawState getNew() {
        return new Idling();
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
        return switch (hexState) {
            case BETWEENPATTERNS -> new Idling();
            case JUSTSTARTED -> new AutoCompleting(castui.getStart(),
                    ui.coordToPx(castui.getStart()), ui.hexSize());
            case DRAWING -> new MouseDrawing(castui);
        };
    }

    public static boolean shouldClose(DrawState current) {
        return current.wantsExit;
    }
}
