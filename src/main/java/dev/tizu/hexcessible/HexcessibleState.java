package dev.tizu.hexcessible;

public class HexcessibleState {
    private static boolean shouldPresent = false;
    private static String query = "";

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
}
