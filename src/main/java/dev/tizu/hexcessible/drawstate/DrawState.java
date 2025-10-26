package dev.tizu.hexcessible.drawstate;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.msgs.MsgNewSpellPatternC2S;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import dev.tizu.hexcessible.CastingInterfaceAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Hand;
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

    public static class CastRef {
        private final GuiSpellcasting castui;
        private final Hand handOpenedWith;
        private final List<ResolvedPattern> patterns;
        private final Set<HexCoord> usedSpots;

        public CastRef(GuiSpellcasting castui, Hand handOpenedWith,
                List<ResolvedPattern> patterns, Set<HexCoord> usedSpots) {
            this.castui = castui;
            this.handOpenedWith = handOpenedWith;
            this.patterns = patterns;
            this.usedSpots = usedSpots;
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

        public boolean isUsed(HexCoord coord) {
            return usedSpots.contains(coord);
        }

        public void execute(HexPattern pat, HexCoord start) {
            this.patterns.add(new ResolvedPattern(pat, start, ResolvedPatternType.UNRESOLVED));
            this.usedSpots.addAll(pat.positions(start));
            IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                    new MsgNewSpellPatternC2S(handOpenedWith, pat, patterns));
        }
    }
}
