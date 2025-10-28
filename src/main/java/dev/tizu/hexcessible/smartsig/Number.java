package dev.tizu.hexcessible.smartsig;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexDir;
import dev.tizu.hexcessible.Utils;
import dev.tizu.hexcessible.entries.BookEntries;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Number implements SmartSig {

    @Override
    public @Nullable PatternEntries.Entry get(String query) {
        int num;
        try {
            num = Integer.parseInt(query);
        } catch (NumberFormatException e) {
            return null;
        }

        var pattern = getStrPatternFor(num);
        if (pattern == null)
            return null;
        var angles = Utils.strToAngles(pattern);
        var i18nkey = Text.translatable("hexcessible.smartsig.number").getString();
        return new PatternEntries.Entry(Identifier.of("hexcessible", "number"),
                i18nkey, () -> false, HexDir.EAST, angles, List.of());
    }

    @Override
    public @Nullable BookEntries.Entry getDocumentation(List<HexAngle> sig) {
        var sigstr = Utils.anglesAsStr(sig);
        if (!sigstr.startsWith("AQAA") && !sigstr.startsWith("DEDD"))
            return null;
        return null;
    }

    private @Nullable String getStrPatternFor(int target) {
        if (target == 0)
            return "aqaa";

        String prefix = target >= 0 ? "aqaa" : "dedd";
        double absTarget = Math.abs((double) target);

        // another bfs; how fun
        Queue<State> queue = new LinkedList<>();
        Set<Double> visited = new HashSet<>();

        queue.offer(new State(0.0, ""));
        visited.add(0.0);

        while (!queue.isEmpty() && visited.size() < 40960) {
            State curr = queue.poll();
            if (Math.abs(curr.value - absTarget) < 0.0001)
                return prefix + curr.path;

            tryOp(queue, visited, curr, 'w', curr.value + 1, absTarget);
            tryOp(queue, visited, curr, 'q', curr.value + 5, absTarget);
            tryOp(queue, visited, curr, 'e', curr.value + 10, absTarget);
            if (curr.value > 0)
                tryOp(queue, visited, curr, 'a', curr.value * 2, absTarget);
            if (curr.value > 0)
                tryOp(queue, visited, curr, 'd', curr.value / 2, absTarget);
        }

        return null;
    }

    private void tryOp(Queue<State> queue, Set<Double> visited, State curr,
            char op, double newValue, double target) {
        // don't explore if we're getting too far from target
        if (newValue > target * 2 || visited.contains(newValue))
            return;
        visited.add(newValue);
        queue.offer(new State(newValue, curr.path + op));
    }

    private static record State(double value, String path) {
    }
}
