package dev.tizu.hexcessible.entries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.smartsig.SmartSig.SmartSigRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PatternEntries {
    public static final PatternEntries INSTANCE = new PatternEntries();

    private List<Entry> entries = new ArrayList<>();
    private List<Identifier> perWorld = new ArrayList<>();
    private final Map<Identifier, List<HexAngle>> perWorldCache = new HashMap<>();
    private final Map<String, List<Entry>> fuzzySearchCache = new HashMap<>();

    private PatternEntries() {
        reindex();
    }

    public void reindex() {
        entries.clear();
        perWorld.clear();
        perWorldCache.clear();
        fuzzySearchCache.clear();

        IXplatAbstractions.INSTANCE.getActionRegistry().getKeys().forEach(key -> {
            var item = IXplatAbstractions.INSTANCE.getActionRegistry().get(key);
            var entry = IXplatAbstractions.INSTANCE.getActionRegistry().getEntry(key);

            var id = key.getValue();
            var name = Text.translatable(HexAPI.instance().getActionI18nKey(key)).getString();
            Supplier<Boolean> checkLock = () -> BookEntries.INSTANCE.isLocked(id.toString());
            var dir = item.prototype().getStartDir();
            var sig = List.of(item.prototype().getAngles());
            var impls = BookEntries.INSTANCE.get(id);

            if (entry.get().isIn(HexTags.Actions.PER_WORLD_PATTERN))
                perWorld.add(id);

            entries.add(new Entry(id, name, checkLock, dir, sig, impls));
        });

        populatePerWorldCache();
    }

    private void populatePerWorldCache() {
        Hexcessible.cfg().knownWorldPatterns.forEach(p -> {
            var knownEntry = p.split(" ");
            if (knownEntry.length != 3 || !knownEntry[0].equals(Utils.getWorldContext()))
                return;
            var id = Identifier.tryParse(knownEntry[1]);
            if (id != null) {
                perWorldCache.put(id, Utils.angle(knownEntry[2]));
            }
        });
    }

    public List<Entry> get() {
        // FIXME: when no query, smart sigs won't be included, even when some
        // may work without a query
        return entries;
    }

    /** Fuzzy-filtered search (for autocomplete) */
    public List<Entry> get(String query) {
        if (query == null || query.isEmpty())
            return get();

        if (fuzzySearchCache.containsKey(query))
            return fuzzySearchCache.get(query);

        var entries = new ArrayList<>(this.entries);
        entries.addAll(SmartSigRegistry.get(query));

        var result = new ArrayList<>(entries.stream()
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

        fuzzySearchCache.put(query, result);
        return result;
    }

    public @Nullable Entry getFromSig(List<HexAngle> sig) {
        var smart = SmartSigRegistry.get(sig);
        if (smart != null)
            return smart;
        return entries.stream()
                .filter(e -> e.is(sig))
                .findFirst()
                .orElse(null);
    }

    public @Nullable List<HexAngle> getPerWorldSig(Entry entry) {
        if (!entry.isPerWorld())
            return null;
        return perWorldCache.get(entry.id());
    }

    public void setPerWorldSig(Entry entry, List<HexAngle> sig) {
        if (!entry.isPerWorld())
            throw new IllegalStateException("Tried to set per-world sig for non-per-world pattern");

        var sigStr = Utils.angle(sig);
        var worldCtx = Utils.getWorldContext();
        var entryIdStr = entry.id().toString();

        perWorldCache.put(entry.id(), sig);

        var kgp = new ArrayList<>(Hexcessible.cfg().knownWorldPatterns);
        kgp.removeIf(p -> {
            var parts = p.split(" ");
            return parts.length == 3 && parts[0].equals(worldCtx) && parts[1].equals(entryIdStr);
        });
        kgp.add(worldCtx + " " + entryIdStr + " " + sigStr);
        Hexcessible.cfg().knownWorldPatterns = kgp;
        Hexcessible.cfg().markDirty();

        Hexcessible.LOGGER.info("Learned per-world pattern {}", entry.id());
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

        public boolean isPerWorld() {
            return INSTANCE.perWorld.contains(id);
        }

        public @Nullable List<List<HexAngle>> sig() {
            if (!isPerWorld())
                return sig;
            var pws = INSTANCE.getPerWorldSig(this);
            if (pws != null)
                return List.of(pws);
            return null;
        }

        public boolean is(List<HexAngle> other) {
            var sig = this.sig();
            return sig != null && sig.size() == 1 && other.equals(sig.get(0));
        }
    }
}
