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

public class OverevalNut implements SmartSig.Conditional {

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("overevaluate");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        int amount;
        try {
            amount = Integer.parseInt(query);
        } catch (NumberFormatException e) {
            return null;
        }

        if (amount <= 0 || amount > 16)
            return null;

        return List.of(getEntry(amount));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigStr = Utils.angle(sig);
        if (!sigStr.startsWith("aawdde"))
            return null;

        var suffix = sigStr.substring(6);
        for (char c : suffix.toCharArray())
            if (c != 'w')
                return null;

        int amount = 1 + suffix.length();
        return getEntry(amount);
    }

    private PatternEntries.Entry getEntry(int amount) {
        var patternStr = new StringBuilder("aawdde");
        for (int i = 1; i < amount; i++)
            patternStr.append("w");

        var angles = Utils.angle(patternStr.toString());
        var i18nkey = Text.translatable("hexcasting.special.overevaluate:nut", amount).getString();
        var desc = Text.translatable("overevaluate.page.nut").getString();

        var in = new StringBuilder();
        var out = new StringBuilder();
        for (var i = 0; i <= amount; i++) {
            if (!in.isEmpty())
                in.append(", ");
            in.append(i + 1);
            if (!out.isEmpty())
                out.append(", ");
            if (i != 0)
                out.append(i);
            else
                out.append(amount + 1);
        }

        BookEntries.Entry doc = new BookEntries.Entry("hexcessible:nut/" + amount,
                null, desc, in.toString(), out.toString(), 0);

        return new PatternEntries.Entry(Identifier.of("hexcessible", "nut/" + amount),
                i18nkey, () -> false, HexDir.NORTH_EAST, List.of(angles), List.of(doc), 0);
    }
}
