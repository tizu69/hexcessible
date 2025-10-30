package dev.tizu.hexcessible.smartsig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Number implements SmartSig {

    private static final List<String> NUMBERS = new ArrayList<>();
    static {
        Collections.addAll(NUMBERS, Hexcessible.getAsset("/numbers.txt").split("\n"));
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        float num;
        try {
            num = Float.parseFloat(query);
        } catch (NumberFormatException e) {
            return null;
        }

        var pattern = getFor(num);
        if (pattern == null)
            return null;
        return List.of(getEntry(num));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigstr = Utils.angle(sig);
        if (!sigstr.startsWith("aqaa") && !sigstr.startsWith("dedd"))
            return null;

        var num = getFor(sig.subList(4, sig.size()));
        if (sigstr.startsWith("dedd"))
            num = -num;
        return getEntry(num);
    }

    private PatternEntries.Entry getEntry(float target) {
        var sig = getFor(target);
        var i18nkey = Text.translatable("hexcessible.smartsig.number").getString();
        var doc = new BookEntries.Entry("hexcessible:number", null,
                "(experimental)", "", String.valueOf(target), 0);
        return new PatternEntries.Entry(Identifier.of("hexcessible", "number/"
                + target), i18nkey, () -> false, HexDir.EAST, sig, List.of(doc));
    }

    private float getFor(List<HexAngle> sig) {
        var out = 0f;
        for (HexAngle angle : sig)
            out = switch (angle) {
                case LEFT -> out + 5;
                case FORWARD -> out + 1;
                case RIGHT -> out + 10;
                case LEFT_BACK -> out * 2;
                case RIGHT_BACK -> out / 2f;
                default -> out;
            };
        return out;
    }

    private @Nullable List<List<HexAngle>> getFor(float target) {
        if (target == 0)
            return List.of(Utils.angle("aqaa"));
        var prefix = target >= 0 ? "aqaa" : "dedd";

        if (target == (int) target) {
            var simplePattern = trySimplePattern(Math.abs((int) target), prefix);
            if (simplePattern != null)
                return List.of(simplePattern);
        }

        return decomposeNumber(target);
    }

    private @Nullable List<HexAngle> trySimplePattern(int target, String prefix) {
        if (target > 2000)
            return null;
        if (target == 0)
            return Utils.angle("aqaa");
        var pattern = NUMBERS.get(target - 1);
        if (pattern == null)
            return null;
        return Utils.angle(prefix + pattern);
    }

    private List<List<HexAngle>> decomposeNumber(float target) {
        // is decomposition possible?
        if (Math.abs(target - Math.round(target)) < 0.001) {
            int intTarget = Math.round(target);
            return decomposeInt(intTarget);
        }

        // for non-ints, try to express as a fraction and decompose
        // -> multiply by small powers of 2 to make integer
        for (int denom = 2; denom <= 32; denom *= 2) {
            int numer = Math.round(target * denom);
            if (Math.abs(target - (float) numer / denom) < 0.001) {
                var numerPatterns = decomposeInt(numer);
                var denomPatterns = decomposeInt(denom);
                return combineWithOp(numerPatterns, denomPatterns,
                        Utils.angle("wdedw"));
            }
        }

        // fallback: just decompose as integer
        return decomposeInt(Math.round(target));
    }

    private List<List<HexAngle>> decomposeInt(int target) {
        var absTarget = Math.abs(target);
        // For small numbers, try direct generation first
        if (Math.abs(absTarget) <= 2000) {
            var pattern = trySimplePattern((int) Math.abs(absTarget),
                    target >= 0 ? "aqaa" : "dedd");
            if (pattern != null)
                return List.of(pattern);
        }

        // a^b * c + d or a^b + g
        var bestA = 2;
        var bestE = 2;
        var decomp1 = decomposeIntInner(absTarget, bestA);
        var bestB = decomp1[0];
        var bestC = decomp1[1];
        var bestD = decomp1[2];
        var bestF = decomp1[3];
        var bestG = decomp1[4];

        for (var a = 3; a <= Math.sqrt(absTarget); a++) {
            var decomp = decomposeIntInner(absTarget, a);
            if (decomp[2] < bestD) {
                bestA = a;
                bestB = decomp[0];
                bestC = decomp[1];
                bestD = decomp[2];
            }
            if (decomp[4] < bestG) {
                bestE = a;
                bestF = decomp[3];
                bestG = decomp[4];
            }
        }

        // a^b * c + d
        var opt1 = buildDecomposition(bestA, bestB, bestC, bestD, true);
        // a^b + g
        var opt2 = buildDecomposition(bestE, bestF, 1, bestG, false);
        var result = opt1.size() <= opt2.size() ? opt1 : opt2;

        if (target < 0)
            result = negatePatterns(result);
        return result;
    }

    private int[] decomposeIntInner(int num, int a) {
        var b = (int) Math.floor(Math.log(num) / Math.log(a));
        var aPowB = (int) Math.pow(a, b);
        var c = num / aPowB;
        var d = num - aPowB * c;
        var g = num - aPowB;
        return new int[] { b, c, d, b, g };
    }

    private List<List<HexAngle>> buildDecomposition(int a, int b, int c, int d, boolean includeC) {
        List<List<HexAngle>> result = new ArrayList<>();
        result.addAll(decomposeInt(a));
        result.addAll(decomposeInt(b));
        result.add(Utils.angle("wedew")); // ^

        if (includeC && c != 1) {
            result.addAll(decomposeInt(c));
            result.add(Utils.angle("waqaw")); // *
        }
        if (d != 0) {
            result.addAll(decomposeInt(d));
            result.add(Utils.angle("waaw")); // +
        }

        return result;
    }

    private List<List<HexAngle>> combineWithOp(List<List<HexAngle>> left, List<List<HexAngle>> right,
            List<HexAngle> op) {
        List<List<HexAngle>> result = new ArrayList<>();
        result.addAll(left);
        result.addAll(right);
        result.add(op);
        return result;
    }

    private List<List<HexAngle>> negatePatterns(List<List<HexAngle>> patterns) {
        if (patterns.size() == 1) {
            var sig = Utils.angle(patterns.get(0));
            if (sig.startsWith("aqaa"))
                return List.of(Utils.angle("dedd" + sig.substring(4)));
            else if (sig.startsWith("dedd"))
                return List.of(Utils.angle("aqaa" + sig.substring(4)));
        }

        // compound expressions
        List<List<HexAngle>> result = new ArrayList<>(patterns);
        result.add(Utils.angle("deddw"));
        result.add(Utils.angle("waqaw"));
        return result;
    }
}
