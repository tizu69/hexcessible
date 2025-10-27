package dev.tizu.hexcessible;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = Hexcessible.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/amethyst_block.png")
public class HexcessibleConfig implements ConfigData {
    private HexcessibleConfig() {
    }

    @ConfigEntry.Gui.Tooltip
    public boolean dimmed = false;
    @ConfigEntry.Gui.Tooltip
    public boolean hideFloaties = false;
    @ConfigEntry.Gui.Tooltip
    public boolean showAllDots = false;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip
    public boolean inspectMouse = true;

    @ConfigEntry.Gui.NoTooltip
    public boolean debug = false;

    @Override
    public void validatePostLoad() throws ValidationException {
    }
}
