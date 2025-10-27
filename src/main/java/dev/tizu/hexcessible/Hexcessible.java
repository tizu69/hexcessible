package dev.tizu.hexcessible;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class Hexcessible {
    public static final String MOD_ID = "hexcessible";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static HexcessibleConfig cfg;

    public static HexcessibleConfig cfg() {
        if (cfg == null) {
            AutoConfig.register(HexcessibleConfig.class, GsonConfigSerializer::new);
            cfg = AutoConfig.getConfigHolder(HexcessibleConfig.class).getConfig();
        }
        return cfg;
    }
}