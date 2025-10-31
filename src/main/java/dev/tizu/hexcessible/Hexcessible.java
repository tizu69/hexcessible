package dev.tizu.hexcessible;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hexcessible {
    private Hexcessible() {
    }

    public static final String MOD_ID = "hexcessible";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static HexcessibleConfig cfg = HexcessibleConfig.get();

    public static HexcessibleConfig cfg() {
        return cfg;
    }

    public static String getAsset(String path) {
        try (var scanner = new Scanner(
                Hexcessible.class.getResourceAsStream(path),
                StandardCharsets.UTF_8).useDelimiter("\\A")) {
            return scanner.next();
        }
    }
}