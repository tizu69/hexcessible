package dev.tizu.hexcessible.smartsig;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import dev.tizu.hexcessible.entries.PatternEntries;

public interface SmartSig {
    @Nullable
    List<PatternEntries.Entry> get(String query);

    @Nullable
    PatternEntries.Entry get(List<HexAngle> sig);

    public interface Conditional extends SmartSig {
        /**
         * Return true if this SmartSig is enabled, e.g. if it is added by a specific
         * mod, the mod that provides it is installed. Will be run once, on init.
         */
        boolean enabled();
    }

    public static class SmartSigRegistry {
        private SmartSigRegistry() {
        }

        private static final List<SmartSig> REGISTRY = new ArrayList<>();

        static void register(SmartSig sig) {
            if (sig instanceof Conditional cond && !cond.enabled())
                return;
            REGISTRY.add(sig);
        }

        public static List<PatternEntries.Entry> get(String query) {
            var out = new ArrayList<PatternEntries.Entry>();
            for (SmartSig sig : REGISTRY) {
                var entry = sig.get(query);
                if (entry != null)
                    out.addAll(entry);
            }
            return out;
        }

        public static @Nullable PatternEntries.Entry get(List<HexAngle> sig) {
            for (SmartSig ssig : REGISTRY) {
                var entry = ssig.get(sig);
                if (entry != null)
                    return entry;
            }
            return null;
        }

        static {
            register(new Number());
            register(new Bookkeeper());
            register(new Escape());
        }
    }
}
