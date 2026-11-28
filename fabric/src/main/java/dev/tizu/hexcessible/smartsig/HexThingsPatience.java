package dev.tizu.hexcessible.smartsig;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HexThingsPatience implements SmartSig.Conditional {

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("hexthings");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        query = query.toLowerCase();
        if (!query.startsWith("noop") && !query.startsWith("patience"))
            return List.of(getEntry(""));

        var parts = query.split(" ");
        if (parts.length > 2)
            return null;

        var tail = parts.length == 1 ? "" : parts[1];
        if (!tail.matches("[wedaq]*"))
            return null;

        if (!Utils.isValidPattern(Utils.angle("dade" + tail)))
            return null;

        return List.of(getEntry(tail));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigStr = Utils.angle(sig);
        if (!sigStr.startsWith("dade"))
            return null;

        return getEntry(sigStr.substring(4));
    }

    private PatternEntries.Entry getEntry(String tail) {
        var patternStr = "dade" + tail;

        var angles = Utils.angle(patternStr);
        var i18nkey = Component.translatable("hexcasting.special.hexthings:noop").getString();
        var desc = Component.translatable("hexthings.book.patterns.spells.utils.noop").getString();

        BookEntries.Entry doc = new BookEntries.Entry("hexcessible:noop_patience/" + tail,
                null, desc, "", "", 0);

        return new PatternEntries.Entry("hexcessible:noop_patience/" + tail, i18nkey,
                () -> false, HexDir.NORTH_EAST, List.of(angles), List.of(doc), 0);
    }
}
