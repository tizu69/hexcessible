package dev.tizu.hexcessible.smartsig;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Bookkeeper implements SmartSig {
    // For refernece: In this class, true = drop, false = keep
    // This could probably be rewritten to be more better, but as of right now,
    // we just store if the last one was a drop or not to infer the next angle.
    // FIXME: A refactor may be badly needed.

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        List<Boolean> target = new ArrayList<>();
        for (var i = 0; i < query.length(); i++) {
            var ch = query.charAt(i);
            if (ch == 'v' || ch == 'V')
                target.add(true);
            else if (ch == '-' || ch == '_')
                target.add(false);
            else
                return null;
        }
        return List.of(getEntry(target));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        sig = new ArrayList<>(sig);
        List<Boolean> target = new ArrayList<>();

        var lastWasDrop = false;
        if (sig.get(0).equals(HexAngle.LEFT_BACK)) {
            target.add(true);
            sig.remove(0);
            lastWasDrop = true;
        } else {
            target.add(false); // ?????
        }

        var i = 0;
        while (i < sig.size()) {
            var dir = sig.get(i);
            var next = i + 1 < sig.size() ? sig.get(i + 1) : sig.get(i);
            if (dir.equals(lastWasDrop ? HexAngle.RIGHT : HexAngle.FORWARD)) {
                target.add(false);
                lastWasDrop = false;
                i += 1;
            } else if (dir.equals(lastWasDrop ? HexAngle.RIGHT_BACK : HexAngle.RIGHT)
                    && next.equals(HexAngle.LEFT_BACK)) {
                target.add(true);
                lastWasDrop = true;
                i += 2; // skip next, the LEFT_BACK one
            } else
                return null;
        }
        return getEntry(target);
    }

    private static PatternEntries.Entry getEntry(List<Boolean> target) {
        var angles = new ArrayList<HexAngle>();
        var in = new StringBuilder();
        var out = new StringBuilder();

        for (var i = 0; i < target.size(); i++) {
            var lastWasDrop = i > 0 && target.get(i - 1);

            if (Boolean.TRUE.equals(target.get(i))) { // drop
                angles.add(angDrop(lastWasDrop));
                angles.add(HexAngle.LEFT_BACK);

                if (!in.isEmpty())
                    in.append(", ");
                in.append("_");
            } else { // keep
                angles.add(angKeep(lastWasDrop));

                if (!in.isEmpty())
                    in.append(", ");
                if (!out.isEmpty())
                    out.append(", ");
                in.append(i + 1);
                out.append(i + 1);
            }
        }

        angles.remove(0); // The first one we ignore, as it'll be handled by the
        // direction of the pattern, not something we can control with SmartSig.

        var representation = new StringBuilder();
        for (var i = 0; i < target.size(); i++)
            representation.append(Boolean.TRUE.equals(target.get(i)) ? "v" : "-");

        var i18nkey = Text.translatable("hexcessible.smartsig.bookkeeper",
                target.size()).getString();
        var doc = new BookEntries.Entry("hexcessible:bookkeeper", null,
                getDesc(target), in.toString(), out.toString(), 0);
        return new PatternEntries.Entry(Identifier.of("hexcessible",
                "bookkeeper/" + representation), i18nkey, () -> false,
                HexDir.EAST, List.of(angles), List.of(doc), 0);
    }

    private static String getDesc(List<Boolean> target) {
        var firstIsDrop = target.get(0);
        var counts = new ArrayList<Integer>();

        // Count the number of drops/keeps. This is initially !firstIsDrop
        // because in the if below, we want to add the first entry to counts.
        var currentIsDrop = !firstIsDrop.booleanValue();
        for (var i = 0; i < target.size(); i++)
            if (currentIsDrop != target.get(i).booleanValue()) {
                counts.add(1);
                currentIsDrop = !currentIsDrop;
            } else {
                var j = counts.size() - 1;
                counts.set(j, counts.get(j) + 1);
            }

        var str = new StringBuilder();
        str.append(Text.translatable("hexcessible.smartsig.bookkeeper.prefix")
                .getString());

        for (var i = 0; i < counts.size(); i++) {
            if (i != 0)
                str.append(Text.translatable("hexcessible.smartsig.bookkeeper.join")
                        .getString());

            currentIsDrop = i % 2 == 0 ? firstIsDrop : !firstIsDrop;
            var count = counts.get(i);
            var key = currentIsDrop ? "hexcessible.smartsig.bookkeeper.drop"
                    : "hexcessible.smartsig.bookkeeper.keep";
            if (count == 1)
                key += "1";
            str.append(Text.translatable(key, count).getString());
        }

        str.append(Text.translatable("hexcessible.smartsig.bookkeeper.suffix")
                .getString());

        return str.toString();
    }

    private static HexAngle angDrop(boolean lastWasDrop) {
        return lastWasDrop ? HexAngle.RIGHT_BACK : HexAngle.RIGHT;
    }

    private static HexAngle angKeep(boolean lastWasDrop) {
        return lastWasDrop ? HexAngle.RIGHT : HexAngle.FORWARD;
    }
}
