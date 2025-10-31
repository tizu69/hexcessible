package dev.tizu.hexcessible.entries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonSyntaxException;

import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import vazkii.patchouli.common.book.BookRegistry;

/**
 * This reads the Patchouli Hex Book by reading the JSON. This may be improved
 * if I figure out how to use the Patchouli API to do this.
 */
public class BookEntries {

    public static final Identifier BOOKID = Identifier.of("hexcasting", "thehexbook");
    public static final BookEntries INSTANCE = new BookEntries();

    private Map<String, List<Entry>> entries = Map.of();
    private Map<String, Supplier<Boolean>> locked = Map.of();

    private BookEntries() {
        reindex();
    }

    public void reindex() {
        var book = BookRegistry.INSTANCE.books.get(BOOKID);
        if (book == null) {
            Hexcessible.LOGGER.error("Book {} not found", BOOKID);
            return;
        }

        var entries = new HashMap<String, List<Entry>>();
        var locked = new HashMap<String, Supplier<Boolean>>();
        book.getContents().entries.forEach((entryid, entry) -> {
            var pagei = new AtomicInteger(0);
            entry.getPages().forEach(page -> {
                var root = page.sourceObject;
                if (root == null)
                    return;
                try {
                    var type = JsonHelper.getString(root, "type");
                    switch (type) {
                        case "hexcasting:pattern":
                            var id = JsonHelper.getString(root, "op_id");
                            if (!locked.containsKey(id))
                                locked.put(id, entry::isLocked);
                            var desc = JsonHelper.getString(root, "text", "");
                            var in = JsonHelper.getString(root, "input", "");
                            var out = JsonHelper.getString(root, "output", "");
                            entries.computeIfAbsent(id, k -> new ArrayList<>())
                                    .add(new Entry(id, entryid, desc, in, out,
                                            pagei.getAndIncrement()));
                            break;
                    }
                } catch (JsonSyntaxException e) {
                    Hexcessible.LOGGER.error("Failed to parse entry {}", entryid, e);
                }
            });
        });
        this.entries = entries;
        this.locked = locked;
    }

    public static record Entry(String id, @Nullable Identifier entryid,
            String desc, String in, String out, int page) {
        public String getArgs() {
            return (in + " -> " + out).strip();
        }

        public String getDesc() {
            return Text.translatable(desc).getString()
                    .replaceAll("\\$\\([^)]*\\)|/\\$", "")
                    .replaceAll("[\\s^]_", " ");
        }
    }

    public List<Entry> get(Identifier id) {
        return entries.getOrDefault(id.toString(), List.of());
    }

    public boolean isLocked(String id) {
        return locked.getOrDefault(id, () -> false).get();
    }

    @Nullable
    public Entry getBookEntryFor(String id) {
        return entries.getOrDefault(id, List.of()).stream().findFirst().orElse(null);
    }
}
