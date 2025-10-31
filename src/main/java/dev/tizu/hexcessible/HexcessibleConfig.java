package dev.tizu.hexcessible;

import org.jetbrains.annotations.NotNull;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry.Translatable;

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
    @ConfigEntry.Gui.Tooltip
    public boolean keyDocs = false; // TODO: customizable keybind
    @ConfigEntry.Gui.Tooltip
    public boolean uppercaseSig = false;

    @ConfigEntry.Gui.CollapsibleObject
    public Idle idle = new Idle();
    @ConfigEntry.Gui.CollapsibleObject
    public MouseDraw mouseDraw = new MouseDraw();
    @ConfigEntry.Gui.CollapsibleObject
    public KeyboardDraw keyboardDraw = new KeyboardDraw();
    @ConfigEntry.Gui.CollapsibleObject
    public AutoComplete autoComplete = new AutoComplete();

    @ConfigEntry.Gui.NoTooltip
    public boolean debug = false;

    public static class Idle {
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public OptionalTooltip tooltip = OptionalTooltip.DESCRIPTIVE;
    }

    public static class MouseDraw {
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public OptionalTooltip tooltip = OptionalTooltip.DESCRIPTIVE;
    }

    public static class KeyboardDraw {
        @ConfigEntry.Gui.NoTooltip
        public boolean allow = true;
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public ForcedTooltip tooltip = ForcedTooltip.DESCRIPTIVE;
        @ConfigEntry.Gui.Tooltip
        public boolean keyHint = false;
    }

    public static class AutoComplete {
        @ConfigEntry.Gui.NoTooltip
        public boolean allow = true;
        @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
        public OptionalTooltip tooltip = OptionalTooltip.DESCRIPTIVE;
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 3, max = 20)
        public int count = 7;
    }

    public interface Tooltip extends Translatable {
        boolean visible();

        boolean descriptive();
    }

    public enum ForcedTooltip implements Tooltip {
        SIMPLE, DESCRIPTIVE;

        public boolean descriptive() {
            return this == DESCRIPTIVE;
        }

        public boolean visible() {
            return true;
        }

        @Override
        public @NotNull String getKey() {
            return "text.autoconfig.hexcessible.enum.tooltip." + this.name();
        }
    }

    public enum OptionalTooltip implements Tooltip {
        HIDDEN, SIMPLE, DESCRIPTIVE;

        public boolean descriptive() {
            return this == DESCRIPTIVE;
        }

        public boolean visible() {
            return this != HIDDEN;
        }

        @Override
        public @NotNull String getKey() {
            return "text.autoconfig.hexcessible.enum.tooltip." + this.name();
        }
    }

    @Override
    public void validatePostLoad() throws ValidationException {
    }
}
