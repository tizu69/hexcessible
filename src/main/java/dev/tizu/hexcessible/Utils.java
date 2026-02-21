package dev.tizu.hexcessible;

import java.util.ArrayList;
import java.util.List;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

public class Utils {
    private Utils() {
    }

    public static int fluffySearch(String query, String candidate) {
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

    public static boolean isValidPattern(List<HexAngle> angles) {
        var pat = new HexPattern(HexDir.EAST, new ArrayList<>());
        for (var angle : angles) {
            var dir = pat.finalDir();
            if (!pat.tryAppendDir(dir.rotatedBy(angle)))
                return false;
        }
        return true;
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

    public static HexAngle angle(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'q' -> HexAngle.LEFT;
            case 'w' -> HexAngle.FORWARD;
            case 'e' -> HexAngle.RIGHT;
            case 'a' -> HexAngle.LEFT_BACK;
            case 's' -> HexAngle.BACK;
            case 'd' -> HexAngle.RIGHT_BACK;
            default -> throw new IllegalStateException(c + " invalid");
        };
    }

    public static List<HexAngle> angle(String angles) {
        return angles.chars().mapToObj(c -> angle((char) c)).toList();
    }

    public static String angle(HexAngle angle) {
        return switch (angle) {
            case LEFT -> "q";
            case FORWARD -> "w";
            case RIGHT -> "e";
            case LEFT_BACK -> "a";
            case BACK -> "s";
            case RIGHT_BACK -> "d";
        };
    }

    public static String angle(List<HexAngle> angles) {
        return angles.stream().map(Utils::angle).reduce("", String::concat);
    }

    public static String angle(List<HexAngle> angles, boolean uppercase) {
        if (uppercase)
            return angle(angles).toUpperCase();
        return angle(angles);
    }

    public static HexDir[] hexDirs() {
        return HexDir.values();
    }

    public static HexDir[] hexDirs(HexDir start) {
        var dirs = new HexDir[HexDir.values().length];
        for (int i = 0; i < dirs.length; i++)
            dirs[i] = HexDir.values()[(start.ordinal() + i) % HexDir.values().length];
        return dirs;
    }

    private static final String WORLD_CONTEXT_RGX = "[^a-zA-Z0-9]";

    public static String getWorldContext() {
        var worldSolo = MinecraftClient.getInstance().getServer();
        if (worldSolo != null)
            return worldSolo.getSavePath(WorldSavePath.ROOT).normalize()
                    .getFileName().toString().replaceAll(WORLD_CONTEXT_RGX, "_");

        var worldMulti = MinecraftClient.getInstance().getCurrentServerEntry();
        if (worldMulti != null)
            return "multiplayer__" + worldMulti.name.replaceAll(WORLD_CONTEXT_RGX, "_");

        return "unknown__";
    }
}
