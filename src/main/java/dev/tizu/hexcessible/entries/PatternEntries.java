package dev.tizu.hexcessible.entries;

import java.util.ArrayList;
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

    private PatternEntries() {
        reindex();
    }

    public void reindex() {
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
    }

    public List<Entry> get() {
        return entries;
    }

    /** Fuzzy-filtered search (for autocomplete) */
    public List<Entry> get(String query) {
        if (query == null || query.isEmpty())
            return get();

        var entries = new ArrayList<>(this.entries);
        entries.addAll(SmartSigRegistry.get(query));

        return new ArrayList<>(entries.stream()
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
    }

    public @Nullable Entry getFromSig(List<HexAngle> sig) {
        var smart = SmartSigRegistry.get(sig);
        if (smart != null)
            return smart;
        return entries.stream()
                .filter(e -> e.equals(sig))
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

        public boolean isPerWorld() {
            return INSTANCE.perWorld.contains(id);
        }

        public @Nullable List<List<HexAngle>> sig() {
            if (!isPerWorld())
                return sig;

            var pws = getPerWorldSig();
            if (pws != null)
                return List.of(pws);
            return null;
        }

        public @Nullable List<HexAngle> getPerWorldSig() {
            if (!isPerWorld())
                throw new IllegalStateException("Requested per-world sig for non-per-world pattern");
            var known = Hexcessible.cfg().knownWorldPatterns.stream()
                    .filter(p -> {
                        var knownEntry = p.split(" ");
                        if (knownEntry.length != 3 || !knownEntry[0]
                                .equals(Utils.getWorldContext()))
                            return false;
                        var id = Identifier.tryParse(p.split(" ")[1]);
                        return id != null && id.equals(this.id);
                    }).findFirst();
            if (known.isPresent())
                return Utils.angle(known.get().split(" ")[2]);
            return null;
        }

        public void setPerWorldSig(List<HexAngle> sig) {
            if (!isPerWorld())
                throw new IllegalStateException("Tried to set per-world sig for non-per-world pattern");

            var current = getPerWorldSig();
            if (current != null) {
                if (current.equals(sig))
                    return;
                Hexcessible.cfg().knownWorldPatterns.remove(getPerWorldSig(current));
            }

            Hexcessible.LOGGER.info("Learned per-world pattern {}", id);

            // we need to shallow copy the list as it may be an immutable list,
            // and we need to be able to modify it. is there a better way?
            var kgp = new ArrayList<>(Hexcessible.cfg().knownWorldPatterns);
            kgp.add(getPerWorldSig(sig));
            Hexcessible.cfg().knownWorldPatterns = kgp;
            Hexcessible.cfg().markDirty();
        }

        private String getPerWorldSig(List<HexAngle> sig) {
            return Utils.getWorldContext() + " " + id + " " + Utils.angle(sig);
        }

        public boolean equals(Object other) {
            if (!(other instanceof List<?> list))
                return false;
            var sig = this.sig();
            return sig != null && sig.size() == 1 && list.equals(sig.get(0));
        }
    }
}
