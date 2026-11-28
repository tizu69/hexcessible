package dev.tizu.hexcessible;

import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.math.HexSignature;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;

import java.util.List;

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

    public static HexDir finalDir(HexPattern pattern) {
        var dir = pattern.getOrientation();
        for (var angle : pattern.getSignature()) dir = dir.rotatedBy(angle);
        return dir;
    }

    public static boolean canAppendDir(HexPattern pattern, HexDir nextDir) {
        try {
            var builder = new HexSignature.Builder(true);
            for (var a : pattern.getSignature()) builder.addAngle(a);
            var fd = finalDir(pattern);
            var needed = fd.angleFrom(nextDir);
            builder.addAngle(needed);
            builder.build();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canAppendAngle(HexPattern pattern, HexAngle angle) {
        return canAppendDir(pattern, finalDir(pattern).rotatedBy(angle));
    }

    public static boolean isValidPattern(List<HexAngle> angles) {
        try {
            var builder = new HexSignature.Builder(true);
            for (var angle : angles) builder.addAngle(angle);
            builder.build();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static HexCoord finalPos(HexCoord start, HexPattern pattern) {
        var dir = pattern.getOrientation();
        start = start.plus(dir.asDelta());
        for (var angle : pattern.getSignature()) {
            dir = dir.rotatedBy(angle);
            start = start.plus(dir.asDelta());
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

    // qeadzc translate into top-left to bottom-right angles of the hexagon,
    // absolute. An angle relative to the last direction is returned
    public static HexAngle angle(char c, HexDir lastDir) {
        return switch (Character.toLowerCase(c)) {
            case 'q' -> HexDir.NORTH_WEST.angleFrom(lastDir);
            case 'e' -> HexDir.NORTH_EAST.angleFrom(lastDir);
            case 'a' -> HexDir.WEST.angleFrom(lastDir);
            case 'd' -> HexDir.EAST.angleFrom(lastDir);
            case 'z' -> HexDir.SOUTH_WEST.angleFrom(lastDir);
            case 'c' -> HexDir.SOUTH_EAST.angleFrom(lastDir);
            default -> throw new IllegalStateException(c + " invalid");
        };
    }

    public static List<HexAngle> angle(String angles) {
        return angles.chars().mapToObj(c -> angle((char) c)).toList();
    }

    public static char absAngle(HexDir dir) {
        return switch (dir) {
            case NORTH_WEST -> 'q';
            case NORTH_EAST -> 'e';
            case WEST -> 'a';
            case EAST -> 'd';
            case SOUTH_WEST -> 'z';
            case SOUTH_EAST -> 'c';
        };
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
        var worldSolo = Minecraft.getInstance().getSingleplayerServer();
        if (worldSolo != null)
            return worldSolo.getWorldPath(LevelResource.ROOT).normalize()
                    .getFileName().toString().replaceAll(WORLD_CONTEXT_RGX, "_");

        var worldMulti = Minecraft.getInstance().getCurrentServer();
        if (worldMulti != null)
            return "multiplayer__" + worldMulti.name.replaceAll(WORLD_CONTEXT_RGX, "_");

        return "unknown__";
    }
}
