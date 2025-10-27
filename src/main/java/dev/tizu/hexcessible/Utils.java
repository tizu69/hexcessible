package dev.tizu.hexcessible;

import at.petrak.hexcasting.api.casting.math.HexCoord;

public class Utils {
    private Utils() {
    }

    public static int fuzzyScore(String query, String candidate) {
        if (query == null || candidate == null || query.isEmpty())
            return 0;

        var q = query.toLowerCase();
        var c = candidate.toLowerCase();
        var score = 0;
        var consecutive = 0;
        var qi = 0;

        for (int ci = 0; ci < c.length() && qi < q.length(); ci++) {
            if (q.charAt(qi) == c.charAt(ci)) {
                score += 10 + consecutive * 5; // consecutive hits
                consecutive++;
                qi++;
            } else {
                consecutive = 0;
            }
        }

        // bonus if query matches from start of word
        if (!candidate.isEmpty() && candidate.toLowerCase().startsWith(q))
            score += 15;
        return qi == q.length() ? score : 0;
    }
}
