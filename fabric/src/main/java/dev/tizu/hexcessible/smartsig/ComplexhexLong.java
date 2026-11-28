package dev.tizu.hexcessible.smartsig;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// FIXME: this uses the existing Number smartsig as a backend and simply casts
// the result to a long. This is not ideal, and should eventually be replaced
// with its own generator.
public class ComplexhexLong implements SmartSig.Conditional {

    private static final Number backend = new Number();

    @Override
    public boolean enabled() {
        return FabricLoader.getInstance().isModLoaded("complexhex");
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        try {
            Long.parseLong(query);
        } catch (NumberFormatException e) {
            return null;
        }

        var entries = backend.get(query);
        if (entries == null)
            return null;
        return List.of(getEntry(entries.get(0), true));
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        var sigstr = Utils.angle(sig);
        if (!sigstr.startsWith("awdedwaaw") && !sigstr.startsWith("dwaqwddw"))
            return null;
        var entry = backend.get(sigL2N(sig));
        if (entry == null)
            return null;
        return getEntry(entry, false);
    }

    private PatternEntries.Entry getEntry(PatternEntries.Entry entry, boolean addPurification) {
        var target = entry.id().substring("hexcessible:number/".length());

        var sig = new ArrayList<>(entry.sig());
        if (addPurification)
            sig.add(Utils.angle("wawdedwaaw"));

        var i18nkey = Component.translatable("hexcasting.special.complexhex:long",
                target).getString();
        var doc = new BookEntries.Entry("hexcessible:long", null,
                "(experimental²)", "", String.valueOf(target), 0);
        return new PatternEntries.Entry("hexcessible:long/" + target,
                i18nkey, () -> false, HexDir.EAST, sig, List.of(doc), 1);
    }

    private List<HexAngle> sigL2N(List<HexAngle> sig) {
        var angles = Utils.angle(sig);
        if (!angles.startsWith("awdedwaaw") && !angles.startsWith("dwaqwddw"))
            return List.of();
        var neg = angles.startsWith("dwaqwddw");
        var starter = neg ? "dedd" : "aqaa";
        return Utils.angle(starter + angles.substring(9));
    }
}
