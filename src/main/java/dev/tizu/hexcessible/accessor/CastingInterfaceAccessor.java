package dev.tizu.hexcessible.accessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.math.HexSignature;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class CastingInterfaceAccessor {

    private static final MethodHandles.Lookup mhLookup = MethodHandles.lookup();

    public static final Class<?> PatternDrawState;
    private static final MethodHandle getDrawState;

    public static final Class<?> JustStartedClass;
    public static final MethodHandle getStartJ;

    private static final Class<?> DrawingClass;
    private static final MethodHandle getStartD;
    private static final MethodHandle getWipPattern;
    private static final MethodHandle getOrientation;

    static {
        try {
            PatternDrawState = GuiSpellcasting.class.getDeclaredField("drawState").getType();
            var priv = MethodHandles.privateLookupIn(GuiSpellcasting.class, mhLookup);
            getDrawState = priv.findGetter(GuiSpellcasting.class, "drawState", PatternDrawState);

            var drawStates = PatternDrawState.getDeclaredClasses();
            JustStartedClass = drawStateClass(drawStates, "JustStarted");
            priv = MethodHandles.privateLookupIn(JustStartedClass, mhLookup);
            getStartJ = priv.findGetter(JustStartedClass, "start", HexCoord.class);

            DrawingClass = drawStateClass(drawStates, "Drawing");
            priv = MethodHandles.privateLookupIn(DrawingClass, mhLookup);
            getStartD = priv.findGetter(DrawingClass, "start", HexCoord.class);

            getWipPattern = priv.findGetter(DrawingClass, "wipPattern", HexSignature.Builder.class);
            getOrientation = priv.findGetter(DrawingClass, "orientation", HexDir.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // the draw state classes are private to GuiSpellcasting, so they can't be
    // named in an import; find them by simple name instead? idk if this is a
    // good idea to do it like this but seems to work, yolo
    private static Class<?> drawStateClass(Class<?>[] classes, String simpleName) {
        for (var cls : classes)
            if (cls.getSimpleName().equals(simpleName))
                return cls;
        throw new IllegalStateException("Missing draw state " + simpleName);
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

    // this needs cleanup...
    public HexPattern getPattern() {
        try {
            var drawState = getDrawState.invoke(inst);
            if (drawState == null || !DrawingClass.isInstance(drawState)) return null;
            var builder = getWipPattern.invoke(drawState);
            var orientation = (HexDir) getOrientation.invoke(drawState);

            // build a non-mutating snapshot of the signature
            var builderClass = builder.getClass();
            var priv = MethodHandles.privateLookupIn(builderClass, MethodHandles.lookup());
            var turnsField = priv.findVarHandle(builderClass, "turnsBuilder", IntArrayList.class);
            var currentIntField = priv.findVarHandle(builderClass, "currentInt", int.class);
            var currentTurnIndexField = priv.findVarHandle(builderClass, "currentTurnIndex", int.class);
            var turnsList = (IntArrayList) turnsField.get(builder);
            int currentInt = (int) currentIntField.get(builder);
            int currentTurnIndex = (int) currentTurnIndexField.get(builder);
            
            // copy turns
            int[] packed = new int[turnsList.size() + (currentTurnIndex > 0 ? 1 : 0)];
            for (int i = 0; i < turnsList.size(); i++) packed[i] = turnsList.getInt(i);
            if (currentTurnIndex > 0) packed[turnsList.size()] = currentInt;
            
            // construct signature via reflection
            var cons = HexSignature.class.getDeclaredConstructor(int[].class);
            cons.setAccessible(true);
            var sig = (HexSignature) cons.newInstance(packed);
            return new HexPattern(orientation, sig);
        } catch (Throwable e) {
            Hexcessible.LOGGER.error("Failed to get CastUI drawing state", e);
            return null;
        }
    }

}
