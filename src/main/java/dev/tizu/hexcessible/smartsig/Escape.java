package dev.tizu.hexcessible.smartsig;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Escape implements SmartSig {
    public static final List<PatternEntries.Entry> ALL = List.of(
            make("escape", HexDir.WEST, "qqqaw", "hexcasting.page.patterns_as_iotas.escape.1", " \\"),
            make("open_paren", HexDir.WEST, "qqq", "hexcasting.page.patterns_as_iotas.parens.1", " ({"),
            make("close_paren", HexDir.EAST, "eee", "hexcasting.page.patterns_as_iotas.parens.2", " )}"),
            make("undo", HexDir.EAST, "eeedw", "hexcasting.page.patterns_as_iotas.undo", " /")); // why / ???

    private static PatternEntries.Entry make(String id, HexDir dir, String sig,
            String desc, String nameadd) {
        var doc = new BookEntries.Entry("hexcessible:escape/" + id, null,
                Text.translatable(desc).getString(), "", "", 0);
        var name = Text.translatable("hexcasting.rawhook.hexcasting:" + id)
                .getString() + nameadd;
        return new PatternEntries.Entry(Identifier.of("hexcessible", id), name,
                () -> false, dir, List.of(Utils.angle(sig)), List.of(doc), 0);
    }

    @Override
    public @Nullable List<PatternEntries.Entry> get(String query) {
        return ALL;
    }

    @Override
    public @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
        return ALL.stream()
                .filter(e -> e.sig().stream().anyMatch(s -> s.equals(sig)))
                .findFirst()
                .orElse(null);
    }
}
