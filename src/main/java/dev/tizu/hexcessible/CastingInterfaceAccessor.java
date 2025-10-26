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

    public static final Class<?> JustStartedClass;
    public static final MethodHandle getStartJ;

    private static final Class<?> DrawingClass;
    private static final MethodHandle getStartD;
    private static final MethodHandle getWipPattern;

    static {
        try {
            PatternDrawState = Class.forName("at.petrak.hexcasting.client.gui.GuiSpellcasting$PatternDrawState");
            var priv = MethodHandles.privateLookupIn(GuiSpellcasting.class, mhLookup);
            getDrawState = priv.findGetter(GuiSpellcasting.class, "drawState", PatternDrawState);

            JustStartedClass = Class
                    .forName("at.petrak.hexcasting.client.gui.GuiSpellcasting$PatternDrawState$JustStarted");
            priv = MethodHandles.privateLookupIn(JustStartedClass, mhLookup);
            getStartJ = priv.findGetter(JustStartedClass, "start", HexCoord.class);

            DrawingClass = Class.forName("at.petrak.hexcasting.client.gui.GuiSpellcasting$PatternDrawState$Drawing");
            priv = MethodHandles.privateLookupIn(DrawingClass, mhLookup);
            getStartD = priv.findGetter(DrawingClass, "start", HexCoord.class);
            getWipPattern = priv.findGetter(DrawingClass, "wipPattern", HexPattern.class);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final GuiSpellcasting inst;

    public CastingInterfaceAccessor(GuiSpellcasting inst) {
        this.inst = inst;
    }

    public State getState() {
        try {
            Object drawState = getDrawState.invoke(inst);
            return switch (drawState.getClass().getSimpleName()) {
                case "BetweenPatterns" -> State.BETWEENPATTERNS;
                case "JustStarted" -> State.JUSTSTARTED;
                case "Drawing" -> State.DRAWING;
                default -> throw new IllegalStateException();
            };
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI state", e);
            return State.BETWEENPATTERNS;
        }
    }

    public enum State {
        BETWEENPATTERNS, JUSTSTARTED, DRAWING
    }

    public HexCoord getStart() {
        try {
            return switch (getState()) {
                case JUSTSTARTED ->
                    (HexCoord) getStartJ.invoke(getDrawState.invoke(inst));
                case DRAWING ->
                    (HexCoord) getStartD.invoke(getDrawState.invoke(inst));
                default -> throw new IllegalStateException();
            };
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI drawing state", e);
            return null;
        }
    }

    public HexPattern getPattern() {
        try {
            return (HexPattern) getWipPattern.invoke(getDrawState.invoke(inst));
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI drawing state", e);
            return null;
        }
    }

}
