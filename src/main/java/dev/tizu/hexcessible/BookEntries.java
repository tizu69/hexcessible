package dev.tizu.hexcessible;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonSyntaxException;

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

    private List<Entry> entries = List.of();

    private BookEntries() {
        reindex();
    }

    public void reindex() {
        var book = BookRegistry.INSTANCE.books.get(BOOKID);
        if (book == null) {
            Hexcessible.LOGGER.error("Book {} not found", BOOKID);
            return;
        }

        var entries = new ArrayList<Entry>();
        book.getContents().entries.forEach((entryid, entry) -> entry.getPages().forEach(page -> {
            var root = page.sourceObject;
            // TODO: entry.shouldHide();
            if (root == null)
                return;
            try {
                var type = JsonHelper.getString(root, "type");
                if (!type.equals("hexcasting:pattern"))
                    return;
                var id = JsonHelper.getString(root, "op_id");
                var desc = JsonHelper.getString(root, "text", "");
                var in = JsonHelper.getString(root, "input", "");
                var out = JsonHelper.getString(root, "output", "");
                entries.add(new Entry(id, entryid, desc, in, out));
            } catch (JsonSyntaxException e) {
                Hexcessible.LOGGER.error("Failed to parse entry {}", entryid, e);
            }
        }));
        this.entries = entries;
    }

    public static record Entry(String id, Identifier entryid,
            String desc, String in, String out) {
    }

    public List<Entry> get(Identifier id) {
        var list = new ArrayList<Entry>();
        entries.forEach(e -> {
            if (e.id.equals(id.toString()))
                list.add(e);
        });
        return list;
    }
}
