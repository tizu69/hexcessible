package dev.tizu.hexcessible.smartsig;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;

public interface SmartSig {
    @Nullable
    PatternEntries.Entry get(String query);

    @Nullable
    BookEntries.Entry getDocumentation(List<HexAngle> sig);

    public static class SmartSigRegistry {
        private SmartSigRegistry() {
        }

        private static final List<SmartSig> REGISTRY = new ArrayList<>();

        static void register(SmartSig sig) {
            REGISTRY.add(sig);
        }

        public static List<PatternEntries.Entry> get(String query) {
            var out = new ArrayList<PatternEntries.Entry>();
            for (SmartSig sig : REGISTRY) {
                var entry = sig.get(query);
                if (entry != null)
                    out.add(entry);
            }
            return out;
        }

        static {
            register(new Number());
        }
    }
}
