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
            make("escape", HexDir.WEST, "qqqaw", "hexcasting.page.patterns_as_iotas.escape.1"),
            make("block_open", HexDir.WEST, "qqq", "hexcasting.page.patterns_as_iotas.parens.1"),
            make("block_close", HexDir.EAST, "eee", "hexcasting.page.patterns_as_iotas.parens.2"),
            make("undo", HexDir.EAST, "eeedw", "hexcasting.page.patterns_as_iotas.undo"));

    private static PatternEntries.Entry make(String id, HexDir dir, String sig, String desc) {
        var doc = new BookEntries.Entry("hexcessible:escape/" + id, null,
                Text.translatable(desc).getString(), "", "", 0);
        return new PatternEntries.Entry(Identifier.of("hexcessible", id),
                Text.translatable("hexcessible.smartsig.escape." + id).getString(),
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
