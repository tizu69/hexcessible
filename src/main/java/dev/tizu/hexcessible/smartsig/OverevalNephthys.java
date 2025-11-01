package dev.tizu.hexcessible.smartsig;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OverevalNephthys implements SmartSig.Conditional {

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("overevaluate");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        int depth;
        try {
            depth = Integer.parseInt(query);
        } catch (NumberFormatException e) {
            return null;
        }

        if (depth <= 0)
            return null;

        return List.of(getEntry(depth));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigStr = Utils.angle(sig);
        if (!sigStr.startsWith("deaqqd"))
            return null;

        var suffix = sigStr.substring(6);
        for (var i = 0; i < suffix.length(); i++)
            if (suffix.charAt(i) != "qe".charAt(i % 2))
                return null;

        return getEntry(1 + suffix.length());
    }

    private PatternEntries.Entry getEntry(int depth) {
        var patternStr = new StringBuilder("deaqqd");
        for (var i = 1; i < depth; i++)
            patternStr.append("eq".charAt(i % 2));

        var angles = Utils.angle(patternStr.toString());
        var i18nkey = Text.translatable("hexcasting.special.overevaluate:nephthys", depth).getString();
        var desc = Text.translatable("overevaluate.page.nephthys.summary").getString();

        var in = new StringBuilder();
        var out = new StringBuilder();
        out.append("...");
        for (var i = 0; i < depth; i++) {
            in.append(", ");
            in.append(i + 1);
            if (!out.isEmpty())
                out.append(", ");
            out.append(i + 1);
        }
        in.append(", pattern | [pattern]");

        BookEntries.Entry doc = new BookEntries.Entry("hexcessible:nephthys/" + depth,
                null, desc, in.toString(), out.toString(), 0);

        return new PatternEntries.Entry(Identifier.of("hexcessible", "nephthys/" + depth),
                i18nkey, () -> false, HexDir.SOUTH_EAST, List.of(angles), List.of(doc));
    }
}
