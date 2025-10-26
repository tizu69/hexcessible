package dev.tizu.hexcessible;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;

public class CastingInterfaceAccessor {

    private static final MethodHandles.Lookup mhLookup = MethodHandles.lookup();

    public static final Class<?> PatternDrawState;
    private static final MethodHandle getDrawState;

    private static final Class<?> DrawingClass;
    private static final MethodHandle getStart;
    private static final MethodHandle getWipPattern;

    static {
        try {
            PatternDrawState = Class.forName("at.petrak.hexcasting.client.gui.GuiSpellcasting$PatternDrawState");
            var priv = MethodHandles.privateLookupIn(GuiSpellcasting.class, mhLookup);
            getDrawState = priv.findGetter(GuiSpellcasting.class, "drawState", PatternDrawState);

            DrawingClass = Class.forName("at.petrak.hexcasting.client.gui.GuiSpellcasting$PatternDrawState$Drawing");
            priv = MethodHandles.privateLookupIn(DrawingClass, mhLookup);
            getStart = priv.findGetter(DrawingClass, "start", HexCoord.class);
            getWipPattern = priv.findGetter(DrawingClass, "wipPattern", HexPattern.class);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final GuiSpellcasting inst;

    public CastingInterfaceAccessor(GuiSpellcasting inst) {
        this.inst = inst;
    }

    public boolean isDrawing() {
        try {
            Object drawState = getDrawState.invoke(inst);
            String className = drawState.getClass().getSimpleName();
            return className.equals("Drawing");
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to check CastUI state", e);
            return false;
        }
    }

    public boolean isIdle() {
        try {
            Object drawState = getDrawState.invoke(inst);
            String className = drawState.getClass().getSimpleName();
            return className.equals("BetweenPatterns");
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to check CastUI state", e);
            return false;
        }
    }

    public HexCoord getStart() {
        try {
            return (HexCoord) getStart.invoke(getDrawState.invoke(inst));
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI state", e);
            return null;
        }
    }

    public HexPattern getPattern() {
        try {
            return (HexPattern) getWipPattern.invoke(getDrawState.invoke(inst));
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI state", e);
            return null;
        }
    }

}
