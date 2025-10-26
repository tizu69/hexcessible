package dev.tizu.hexcessible;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = Hexcessible.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/amethyst_block.png")
public class HexcessibleConfig implements ConfigData {
    private HexcessibleConfig() {
    }

    private static HexcessibleConfig instance;

    public static HexcessibleConfig get() {
        if (instance == null) {
            AutoConfig.register(HexcessibleConfig.class, GsonConfigSerializer::new);
            instance = AutoConfig.getConfigHolder(HexcessibleConfig.class).getConfig();
        }
        return instance;
    }

    @ConfigEntry.Gui.Tooltip
    public boolean dimCastUI = false;
    @ConfigEntry.Gui.Tooltip
    public boolean hideFloaties = false;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean inspectMouse = true;

    @ConfigEntry.Gui.NoTooltip
    public boolean debug = false;

    @Override
    public void validatePostLoad() throws ValidationException {
    }
}
