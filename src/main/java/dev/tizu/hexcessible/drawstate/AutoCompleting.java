package dev.tizu.hexcessible.drawstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.lwjgl.glfw.GLFW;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;

public final class AutoCompleting extends DrawState {
    private HexCoord start;
    private Vec2f anchor;
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
    private List<PatternEntries.Entry> opts;
    private List<PatternEntries.Entry> optsWithLocked;
    private boolean lastInteractWasMouse = true;
    private Vec2f mousePos = new Vec2f(0, 0);

    public AutoCompleting(CastRef castref, HexCoord start) {
        super(castref);
        this.start = start;

        this.anchor = castref.coordToPx(start);
        this.breakoutSize = (float) Math.pow(castref.hexSize() * 1.75, 2);

        optsWithLocked = PatternEntries.INSTANCE.get();
        opts = new ArrayList<>(optsWithLocked);
        opts.removeIf(PatternEntries.Entry::locked);
    }

    @Override
    public void onCharType(char chr) {
        setQuery(query + chr);
    }

    @Override
    public void onKeyPress(int keyCode, int modifiers) {
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
                if (opts.isEmpty())
                    return;
                nextState = new KeyboardDrawing(castref, opts.get(chosen).sig(), start);
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
            default:
        }
    }

    @Override
    public void onMouseMove(double mx, double my) {
        mousePos = new Vec2f((float) mx, (float) my);
        lastInteractWasMouse = true;

        if (!noDistract())
            return;
        if (mousePos.distanceSquared(anchor) > breakoutSize)
            requestExit();
    }

    @Override
    public List<String> getDebugInfo() {
        return List.of("Breakout: " + mousePos.distanceSquared(anchor)
                + " < " + breakoutSize);
    }

    private void setQuery(String query) {
        if (!Hexcessible.cfg().autoComplete.allow)
            return;
        this.query = query;
        optsWithLocked = PatternEntries.INSTANCE.get(query);
        opts = new ArrayList<>(optsWithLocked);
        opts.removeIf(PatternEntries.Entry::locked);
        chosen = 0;
        chosenDoc = 0;
    }

    private void offsetChosen(int by) {
        var size = opts.size();
        chosen = ((chosen + by) % size + size) % size;
        chosenDoc = 0;
    }

    private void offsetChosenDoc(int by) {
        var size = opts.get(chosen).impls().size();
        chosenDoc = ((chosenDoc + by) % size + size) % size;
    }

    @Override
    public void onRender(DrawContext ctx, int mx, int my) {
        if (!Hexcessible.cfg().autoComplete.allow)
            return;
        var x = (int) anchor.x;
        var y = (int) anchor.y;
        renderQueryTooltip(ctx, x, y, mx, my);
        if (opts.isEmpty() || noDistract())
            return;
        renderAutocompleteTooltips(ctx, x, y);
    }

    private void renderQueryTooltip(DrawContext ctx, int x, int y, int mx, int my) {
        var tr = MinecraftClient.getInstance().textRenderer;
        var tInput = !query.equals("")
                ? Text.literal(query)
                        .append(Text.literal(" " + opts.size())
                                .formatted(Formatting.DARK_GRAY))
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, tInput, noDistract() ? mx : x, noDistract() ? my : y);
    }

    private boolean noDistract() {
        return lastInteractWasMouse && query.isEmpty();
    }

    private void renderAutocompleteTooltips(DrawContext ctx, int x, int y) {
        List<Text> options = prepareOptions();
        List<OrderedText> descLines = prepareDescription();
        drawTooltips(ctx, x, y, options, descLines);
    }

    private List<Text> prepareOptions() {
        var count = Hexcessible.cfg().autoComplete.count;
        var previs = count < 3 ? 0 : 2; // amount of options to show above chosen
        var optsStart = Math.max(0, Math.min(chosen - previs, opts.size() - count));
        var optsEnd = Math.min(opts.size(), optsStart + count);
        List<Text> options = IntStream.range(optsStart, optsEnd)
                .mapToObj(i -> {
                    var picked = i == chosen;
                    var fmt = picked ? Formatting.BLUE : Formatting.GRAY;
                    return Text.literal(opts.get(i).toString()).formatted(fmt);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        var lockedN = optsWithLocked.size() - opts.size();
        if (lockedN > 0)
            options.add(Text.translatable("hexcessible.count_locked",
                    lockedN).formatted(Formatting.DARK_GRAY));
        return options;
    }

    private List<OrderedText> prepareDescription() {
        if (!Hexcessible.cfg().autoComplete.tooltip.visible())
            return List.of();

        var tr = MinecraftClient.getInstance().textRenderer;
        var opt = opts.get(chosen);

        if (!Hexcessible.cfg().autoComplete.tooltip.descriptive()) {
            var text = Text.empty().formatted(Formatting.DARK_GRAY);
            var first = true;
            for (var impl : opt.impls()) {
                if (first)
                    first = false;
                else
                    text.append(Text.literal("\n"));
                text.append(Text.literal(impl.getArgs()));
            }
            return tr.wrapLines(text, 170);
        }

        if (chosenDoc >= opt.impls().size())
            return List.of();
        var docN = "[" + (chosenDoc + 1) + "/" + opt.impls().size() + "]";
        var impl = opt.impls().get(chosenDoc);
        var description = Text.literal(docN + " " + impl.getArgs()).formatted(Formatting.GRAY)
                .append(Text.literal("\n" + impl.getDesc()).formatted(Formatting.DARK_GRAY));

        return tr.wrapLines(description, 170);
    }

    private void drawTooltips(DrawContext ctx, int mx, int my, List<Text> options, List<OrderedText> descLines) {
        var tr = MinecraftClient.getInstance().textRenderer;

        var descH = descLines.size() * (tr.fontHeight + 1);
        var descW = descLines.stream().mapToInt(tr::getWidth).max().orElse(0);
        var optsH = options.size() * (tr.fontHeight + 1);
        var optsW = options.stream().mapToInt(tr::getWidth).max().orElse(0);
        var renderAbove = ctx.getScaledWindowHeight() - my < Math.max(descH, optsH) + 15;
        var descLeft = ctx.getScaledWindowWidth() - mx - optsW < descW + 30;
        var fontH = tr.fontHeight + 1;

        var optionsX = mx + optsW + 20 > ctx.getScaledWindowWidth()
                ? ctx.getScaledWindowWidth() - optsW - 20
                : mx;
        var optionsY = renderAbove ? my - (options.size() * fontH) - 9 : my + 17;
        ctx.drawTooltip(tr, options, optionsX, optionsY);

        if (descLines.isEmpty())
            return;
        var descriptionY = renderAbove ? my - (descLines.size() * fontH) - 9 : my + 17;
        var descriptionX = descLeft ? optionsX - descW - 9 : optionsX + optsW + 9;
        ctx.drawTooltip(tr, descLines, HoveredTooltipPositioner.INSTANCE, descriptionX, descriptionY);
    }

    @Override
    public boolean allowStartDrawing() {
        return noDistract();
    }

    @Override
    public void onMousePress(double mx, double my, int button) {
        // TODO: mouse-based interaction
        if (button == 0)
            requestExit();
    }
}