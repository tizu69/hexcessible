package dev.tizu.hexcessible.drawstate;

import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class AliasChanging extends DrawState {
    private String alias;
    private final String original;
    private final String sig;
    private final String id;

    public AliasChanging(CastRef castref, PatternEntries.Entry entry) {
        super(castref);
        this.alias = entry.isAliased() ? entry.name() : "";
        this.original = entry.rawName();
        this.sig = entry.toSignature();
        this.id = entry.id();
    }

    @Override
    public void onRender(GuiGraphics ctx, int mx, int my) {
        var tr = Minecraft.getInstance().font;

        var x = ctx.guiWidth() / 3;
        var y = ctx.guiHeight() / 2;

        var originalStr = sig + " " + original;
        var originalT = alias.isBlank()
                ? Component.literal(originalStr).withStyle(ChatFormatting.BLUE)
                : Component.literal(originalStr).withStyle(ChatFormatting.GRAY);
        ctx.renderTooltip(tr, originalT, x, y - 1);

        var aliasT = alias.isBlank()
                ? Component.translatable("hexcessible.start_typing.alias")
                        .withStyle(ChatFormatting.DARK_GRAY)
                : Component.literal(alias)
                        .withStyle(ChatFormatting.BLUE);
        ctx.renderTooltip(tr, aliasT, x, y + 16);
    }

    @Override
    public void onCharType(char chr) {
        alias = alias + chr;
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        var ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (ctrl) { // remove last word
                    var words = alias.split(" ");
                    alias = Arrays.stream(words)
                            .limit(words.length - 1l)
                            .collect(Collectors.joining(" "));
                } else { // remove single character
                    alias = alias.isEmpty() ? ""
                            : alias.substring(0, alias.length() - 1);
                }
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB:
                var map = new HashMap<>(Hexcessible.cfg().patternAliases);
                map.put(id, alias.isBlank() ? original : alias.trim());
                Hexcessible.cfg().patternAliases = map;
                Hexcessible.cfg().markDirty();
                requestExit();
                break;
            default:
        }
    }

    @Override
    public Map<String, String> getHints() {
        var keys = new HashMap<String, String>();

        keys.put("tab/enter", alias.isBlank() ? "alias_off" : "alias");

        return keys;
    }
}