package dev.tizu.hexcessible.entries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.smartsig.SmartSig.SmartSigRegistry;
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
            var dir = item.prototype().getStartDir();
            var sig = List.of(item.prototype().getAngles());
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

        var out = new ArrayList<>(entries.stream()
                .map(e -> {
                    var score = 0;
                    score += Utils.fuzzyScore(query, e.name) * 3; // important!
                    score += Utils.fuzzyScore(query, e.id.toString()
                            .replaceAll("[:_/]", " "));
                    return Map.entry(e, score);
                }).filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .toList());
        for (var smart : SmartSigRegistry.get(query))
            out.add(0, smart);
        return out;
    }

    public @Nullable Entry getFromSig(List<HexAngle> sig) {
        var smart = SmartSigRegistry.get(sig);
        if (smart != null)
            return smart;
        return entries.stream()
                .filter(e -> e.sig.stream().anyMatch(s -> s.equals(sig)))
                .findFirst()
                .orElse(null);
    }

    public static record Entry(Identifier id, String name, Supplier<Boolean> checkLock,
            HexDir dir, List<List<HexAngle>> sig, List<BookEntries.Entry> impls) {
        public boolean locked() {
            return checkLock.get();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (var s : sig)
                sb.append("<").append(dir).append(",")
                        .append(Utils.angle(s).toLowerCase())
                        .append("> ");
            sb.append(name);
            return sb.toString();
        }
    }
}
