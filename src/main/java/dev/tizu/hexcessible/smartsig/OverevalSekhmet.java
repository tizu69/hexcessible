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

public class OverevalSekhmet implements SmartSig.Conditional {

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("overevaluate");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        int preserve;
        try {
            preserve = Integer.parseInt(query);
        } catch (NumberFormatException e) {
            return null;
        }

        if (preserve < 0)
            return null;

        return List.of(getEntry(preserve));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigStr = Utils.angle(sig);
        if (!sigStr.startsWith("qaqdd"))
            return null;

        var suffix = sigStr.substring(5);
        for (int i = 0; i < suffix.length(); i++)
            if (suffix.charAt(i) != "qe".charAt(i % 2))
                return null;

        int preserve = suffix.length();
        return getEntry(preserve);
    }

    private PatternEntries.Entry getEntry(int preserve) {
        var patternStr = new StringBuilder("qaqdd");
        for (int i = 0; i < preserve; i++)
            patternStr.append("qe".charAt(i % 2));

        var angles = Utils.angle(patternStr.toString());
        var i18nkey = Text.translatable("hexcasting.special.overevaluate:sekhmet", preserve).getString();
        var desc = Text.translatable("overevaluate.page.sekhmet.0").getString();

        var in = new StringBuilder();
        var out = new StringBuilder();
        in.append("...");
        for (var i = 0; i < preserve; i++) {
            in.append(", ");
            in.append(i + 1);
            if (!out.isEmpty())
                out.append(", ");
            out.append(i + 1);
        }

        BookEntries.Entry doc = new BookEntries.Entry("hexcessible:sekhmet/" + preserve,
                null, desc, in.toString(), out.toString(), 0);

        return new PatternEntries.Entry(Identifier.of("hexcessible", "sekhmet/" + preserve),
                i18nkey, () -> false, HexDir.WEST, List.of(angles), List.of(doc));
    }
}
