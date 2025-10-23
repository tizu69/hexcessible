package dev.tizu.hexcessible;

public class HexcessibleState {
    private static boolean shouldPresent = false;
    private static String query = "";
    private static int chosen = 0;

    private HexcessibleState() {
    }

    public static boolean getShouldPresent() {
        return shouldPresent;
    }

    public static void setShouldPresent(boolean value) {
        shouldPresent = value;
        query = "";
    }

    public static String getQuery() {
        return query;
    }

    public static void setQuery(String value) {
        query = value;
    }

    public static int getChosen() {
        return chosen;
    }

    public static void setChosen(int value) {
        // TODO: once we store actual options, clamp based on their count
        chosen = Math.min(Math.max(value, 0), 3);
    }
}
