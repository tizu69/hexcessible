package dev.tizu.hexcessible.drawstate;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor.State;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AutoCompleting extends DrawState {
    private HexCoord start;
    private Vec2 anchor;
    /**
     * Simulates the circle after which dragging would snap to a point, to stop
     * autocompleting if mouse moves too far away after stopping drawing. 1.75
     * times bigger than actual circle, to prevent instant breakout if the user
     * clicks right on the edge.
     */
    private float breakoutSize;
    private String query = "";
    private int chosen = 0;
    private int chosenDoc = 0;
    private List<PatternEntries.Entry> suggestions = new ArrayList<>();
    private boolean lastInteractWasMouse = true;
    private Vec2 mousePos = new Vec2(0, 0);

    public AutoCompleting(CastRef castref, HexCoord start) {
        super(castref);
        this.start = start;

        this.anchor = castref.coordToPx(start);
        this.breakoutSize = (float) Math.pow(castref.hexSize() * 1.75, 2);

        suggestions = PatternEntries.INSTANCE.get();
    }

    public AutoCompleting(CastRef castref) {
        this(castref, new HexCoord(0, 0));
    }

    private List<PatternEntries.Entry> getUnlockedSuggestions() {
        return suggestions.stream().filter(e -> !e.locked()).toList();
    }

    @Override
    public void onCharType(char chr) {
        castref.stopDrawing();
        setQuery(query + chr);
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
        var unlocked = getUnlockedSuggestions();
        if (noDistract())
            return; // if no options are shown, no need to provide opt controls.
        lastInteractWasMouse = false;
        var ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (ctrl) { // remove last word
                    var words = query.split(" ");
                    setQuery(Arrays.stream(words)
                            .limit(words.length - 1l)
                            .collect(Collectors.joining(" ")));
                } else { // remove single character
                    setQuery(query.isEmpty() ? ""
                            : query.substring(0, query.length() - 1));
                }
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB:
                if (unlocked.isEmpty())
                    return;
                var sig = unlocked.get(chosen).sig();
                if (sig == null)
                    return;
                var dir = unlocked.get(chosen).dir();
                nextState = new KeyboardDrawing(castref, start, sig, dir);
                break;
            case GLFW.GLFW_KEY_UP:
                offsetChosen(-1);
                break;
            case GLFW.GLFW_KEY_DOWN:
                offsetChosen(1);
                break;
            case GLFW.GLFW_KEY_LEFT:
                offsetChosenDoc(-1);
                break;
            case GLFW.GLFW_KEY_RIGHT:
                offsetChosenDoc(1);
                break;
            case GLFW.GLFW_KEY_E, GLFW.GLFW_KEY_F2:
                if (keyCode == GLFW.GLFW_KEY_E && !ctrl)
                    return;
                nextState = new AliasChanging(castref, unlocked.get(chosen));
                break;
            default:
        }
    }

    @Override
    public void onMouseMove(double mx, double my) {
        mousePos = new Vec2((float) mx, (float) my);
        lastInteractWasMouse = true;

        if (noDistract() && castref.internals().getState() == State.BETWEENPATTERNS
                && mousePos.distanceToSqr(anchor) > breakoutSize)
            requestExit();
    }

    @Override
    public boolean onMouseScroll(int delta) {
        offsetChosen(-delta);
        return true;
    }

    @Override
    public List<String> getDebugInfo() {
        return List.of("Breakout: " + mousePos.distanceToSqr(anchor)
                + " < " + breakoutSize);
    }

    private void setQuery(String query) {
        if (!Hexcessible.cfg().autoComplete.allow)
            return;
        this.query = query;
        suggestions = PatternEntries.INSTANCE.get(query);
        chosen = 0;
        chosenDoc = 0;
    }

    private void offsetChosen(int by) {
        var size = getUnlockedSuggestions().size();
        if (size == 0)
            return;
        chosen = ((chosen + by) % size + size) % size;
        chosenDoc = 0;
    }

    private void offsetChosenDoc(int by) {
        var unlocked = getUnlockedSuggestions();
        if (unlocked.isEmpty())
            return;
        var size = unlocked.get(chosen).impls().size();
        if (size == 0)
            return;
        chosenDoc = ((chosenDoc + by) % size + size) % size;
    }

    @Override
    public void onRender(GuiGraphics ctx, int mx, int my) {
        if (!Hexcessible.cfg().autoComplete.allow)
            return;
        var x = (int) anchor.x;
        var y = (int) anchor.y;
        renderQueryTooltip(ctx, x, y);
        if (getUnlockedSuggestions().isEmpty() || noDistract())
            return;
        renderAutocompleteTooltips(ctx, x, y);
    }

    private void renderQueryTooltip(GuiGraphics ctx, int x, int y) {
        var tr = Minecraft.getInstance().font;
        var unlockedCount = getUnlockedSuggestions().size();
        var tInput = !query.equals("")
                ? Component.literal(query).append(Component.literal(" " + unlockedCount)
                        .withStyle(ChatFormatting.DARK_GRAY))
                : Component.translatable("hexcessible.start_typing")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
        if (!noDistract())
            ctx.renderTooltip(tr, tInput, x, y);
        else if (castref.canTypeHere())
            ctx.drawString(tr, tInput, x + 12, y - 12, 15728880);
    }

    private boolean noDistract() {
        return lastInteractWasMouse && query.isEmpty();
    }

    private void renderAutocompleteTooltips(GuiGraphics ctx, int x, int y) {
        List<Component> options = prepareOptions();
        List<FormattedCharSequence> descLines = prepareDescription();
        renderTooltips(ctx, x, y, options, descLines);
    }

    private List<Component> prepareOptions() {
        var unlocked = getUnlockedSuggestions();
        var count = Hexcessible.cfg().autoComplete.count;
        var previs = count < 3 ? 0 : 2; // amount of options to show above chosen
        var optsStart = Math.max(0, Math.min(chosen - previs, unlocked.size() - count));
        var optsEnd = Math.min(unlocked.size(), optsStart + count);
        List<Component> options = IntStream.range(optsStart, optsEnd)
                .mapToObj(i -> {
                    var picked = i == chosen;
                    var fmt = picked ? ChatFormatting.BLUE : ChatFormatting.GRAY;
                    return Component.literal(unlocked.get(i).toString()).withStyle(fmt);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        var lockedN = suggestions.size() - unlocked.size();
        if (lockedN > 0)
            options.add(Component.translatable("hexcessible.count_locked",
                    lockedN).withStyle(ChatFormatting.DARK_GRAY));
        return options;
    }

    private List<FormattedCharSequence> getDescriptionForSimpleTooltip(PatternEntries.Entry opt) {
        var tr = Minecraft.getInstance().font;
        var text = Component.empty().withStyle(ChatFormatting.DARK_GRAY);
        var first = true;
        for (var impl : opt.impls()) {
            if (first)
                first = false;
            else
                text.append(Component.literal("\n"));
            text.append(Component.literal(impl.getArgs()));
        }
        return tr.split(text, 170);
    }

    private List<FormattedCharSequence> getDescriptionForDescriptiveTooltip(PatternEntries.Entry opt) {
        var tr = Minecraft.getInstance().font;
        if (chosenDoc >= opt.impls().size())
            return List.of();
        var docN = "[" + (chosenDoc + 1) + "/" + opt.impls().size() + "]";
        var impl = opt.impls().get(chosenDoc);
        var description = Component.literal(docN + " " + impl.getArgs()).withStyle(ChatFormatting.GRAY)
                .append(Component.literal("\n" + impl.getDesc()).withStyle(ChatFormatting.DARK_GRAY));
        return tr.split(description, 170);
    }

    private List<FormattedCharSequence> prepareDescription() {
        var tr = Minecraft.getInstance().font;
        var unlocked = getUnlockedSuggestions();
        if (unlocked.isEmpty() || chosen >= unlocked.size())
            return List.of();
        var opt = unlocked.get(chosen);

        if (opt.sig() == null)
            return tr.split(Component.translatable("hexcessible.world_specific_autocomplete")
                    .withStyle(ChatFormatting.RED), 170);

        var tooltipConfig = Hexcessible.cfg().autoComplete.tooltip;
        if (!tooltipConfig.visible())
            return List.of();

        return !tooltipConfig.descriptive()
                ? new ArrayList<>(getDescriptionForSimpleTooltip(opt))
                : new ArrayList<>(getDescriptionForDescriptiveTooltip(opt));
    }

    private void renderTooltips(GuiGraphics ctx, int mx, int my, List<Component> options, List<FormattedCharSequence> descLines) {
        var tr = Minecraft.getInstance().font;

        var descH = descLines.size() * (tr.lineHeight + 1);
        var descW = descLines.stream().mapToInt(tr::width).max().orElse(0);
        var optsH = options.size() * (tr.lineHeight + 1);
        var optsW = options.stream().mapToInt(tr::width).max().orElse(0);
        var renderAbove = ctx.guiHeight() - my < Math.max(descH, optsH) + 15;
        var descLeft = ctx.guiWidth() - mx - optsW < descW + 30;
        var fontH = tr.lineHeight + 1;

        var optionsX = mx + optsW + 20 > ctx.guiWidth()
                ? ctx.guiWidth() - optsW - 20
                : mx;
        var optionsY = renderAbove ? my - (options.size() * fontH) - 9 : my + 17;
        ctx.renderTooltip(tr, options, Optional.empty(), optionsX, optionsY);

        if (descLines.isEmpty())
            return;
        var descriptionY = renderAbove ? my - (descLines.size() * fontH) - 9 : my + 17;
        var descriptionX = descLeft ? optionsX - descW - 9 : optionsX + optsW + 9;
        ctx.renderTooltip(tr, descLines, DefaultTooltipPositioner.INSTANCE, descriptionX, descriptionY);
    }

    @Override
    public boolean allowStartDrawing() {
        return noDistract();
    }

    @Override
    public void requestExit() {
        castref.stopDrawing();
        super.requestExit();
    }

    @Override
    public void onMousePress(double mx, double my, int button) {
        // TODO: mouse-based interaction
        if (button == 0)
            requestExit();
    }

    @Override
    public Map<String, String> getHints() {
        var keys = new HashMap<String, String>();

        keys.put("type", "search");
        if (!noDistract()) {
            keys.put("tab/enter", "cast");
            keys.put("wheel/up/down", "scroll");
            keys.put("left/right", "scroll_definitions");
            keys.put("ctrl-e", "alias");
        }

        return keys;
    }
}