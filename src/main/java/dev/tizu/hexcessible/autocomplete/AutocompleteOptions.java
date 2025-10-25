package dev.tizu.hexcessible.autocomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.tizu.hexcessible.BookEntries;
import dev.tizu.hexcessible.Utils;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AutocompleteOptions {
    public static final AutocompleteOptions INSTANCE = new AutocompleteOptions();

    private List<Entry> entries = new ArrayList<>();

    private AutocompleteOptions() {
        reindex();
    }

    public void reindex() {
        IXplatAbstractions.INSTANCE.getActionRegistry().getKeys().forEach(key -> {
            var item = IXplatAbstractions.INSTANCE.getActionRegistry().get(key);

            var id = key.getValue();
            var name = Text.translatable(HexAPI.instance().getActionI18nKey(key)).getString();
            var dir = item.prototype().component1();
            var sig = item.prototype().anglesSignature();
            var impls = BookEntries.INSTANCE.get(id);
            entries.add(new Entry(id, name, dir, sig, impls));
        });
    }

    public List<Entry> get() {
        return entries;
    }

    /** Fuzzy-filtered search */
    public List<Entry> get(String query) {
        if (query == null || query.isEmpty())
            return get();
        return entries.stream()
                .map(e -> Map.entry(e, Utils.fuzzyScore(query, e.name)))
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .map(Map.Entry::getKey)
                .toList();
    }

    public static record Entry(Identifier id, String name,
            HexDir dir, String sig, List<BookEntries.Entry> impls) {
    }
}
