package dev.tizu.hexcessible;

import java.util.List;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;

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

    public static HexCoord finalPos(HexCoord start, HexPattern pattern) {
        var dir = pattern.getStartDir();
        start = start.plus(dir);
        for (var angle : pattern.getAngles()) {
            dir = dir.rotatedBy(angle);
            start = start.plus(dir);
        }
        return start;
    }

    public static HexAngle charToAngle(char c) {
        return switch (c) {
            case 'q' -> HexAngle.LEFT;
            case 'w' -> HexAngle.FORWARD;
            case 'e' -> HexAngle.RIGHT;
            case 'a' -> HexAngle.LEFT_BACK;
            case 'd' -> HexAngle.RIGHT_BACK;
            case 'Q' -> HexAngle.LEFT;
            case 'W' -> HexAngle.FORWARD;
            case 'E' -> HexAngle.RIGHT;
            case 'A' -> HexAngle.LEFT_BACK;
            case 'D' -> HexAngle.RIGHT_BACK;
            default -> throw new IllegalStateException(c + " invalid");
        };
    }

    public static List<HexAngle> strToAngles(String angles) {
        return angles.chars().mapToObj(c -> charToAngle((char) c)).toList();
    }

    public static String anglesAsStr(List<HexAngle> angles) {
        return angles.stream().map(s -> switch (s) {
            case LEFT -> "q";
            case FORWARD -> "w";
            case RIGHT -> "e";
            case LEFT_BACK -> "a";
            case BACK -> "s";
            case RIGHT_BACK -> "d";
        }).reduce("", String::concat);
    }
}
