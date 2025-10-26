package dev.tizu.hexcessible;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PatternEntries {
    public static final PatternEntries INSTANCE = new PatternEntries();

    private List<Entry> entries = new ArrayList<>();

    private PatternEntries() {
        reindex();
    }

    public void reindex() {
        IXplatAbstractions.INSTANCE.getActionRegistry().getKeys().forEach(key -> {
            var item = IXplatAbstractions.INSTANCE.getActionRegistry().get(key);

            var id = key.getValue();
            var name = Text.translatable(HexAPI.instance().getActionI18nKey(key)).getString();
            Supplier<Boolean> checkLock = () -> BookEntries.INSTANCE.isLocked(id.toString());
            var dir = item.prototype().component1();
            var sig = item.prototype().anglesSignature();
            var impls = BookEntries.INSTANCE.get(id);
            entries.add(new Entry(id, name, checkLock, dir, sig, impls));
        });
    }

    public List<Entry> get() {
        return entries;
    }

    /** Fuzzy-filtered search (for autocomplete) */
    public List<Entry> get(String query) {
        if (query == null || query.isEmpty())
            return get();

        return entries.stream()
                .map(e -> {
                    var score = 0;
                    score += Utils.fuzzyScore(query, e.name) * 3; // important!
                    score += Utils.fuzzyScore(query, e.id.toString());
                    return Map.entry(e, score);
                })
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    @Nullable
    public Entry getFromSig(String sig) {
        return entries.stream()
                .filter(e -> e.sig.equals(sig))
                .findFirst()
                .orElse(null);
    }

    public static record Entry(Identifier id, String name, Supplier<Boolean> checkLock,
            HexDir dir, String sig, List<BookEntries.Entry> impls) {
        public boolean locked() {
            return checkLock.get();
        }

        public String toString() {
            return "<" + dir + "," + sig + "> " + name;
        }
    }
}
