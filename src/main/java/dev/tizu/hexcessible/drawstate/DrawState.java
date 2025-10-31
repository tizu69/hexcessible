package dev.tizu.hexcessible.drawstate;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor;
import kotlin.Pair;
import net.minecraft.client.gui.DrawContext;

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
        return switch (hexState) {
            case BETWEENPATTERNS -> new Idling(current.castref);
            case JUSTSTARTED -> new AutoCompleting(current.castref, accessor.getStart());
            case DRAWING -> new MouseDrawing(current.castref, accessor);
        };
    }

    public static boolean shouldClose(DrawState current) {
        return current.wantsExit;
    }

    public Pair<List<String>, List<String>> getStackMod() {
        return new Pair<>(List.of(), List.of());
    }
}
