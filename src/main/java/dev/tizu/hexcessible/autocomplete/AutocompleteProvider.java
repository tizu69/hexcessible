package dev.tizu.hexcessible.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

import dev.tizu.hexcessible.BookEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutocompleteProvider {
    @Nullable
    private Vector2d presentAt = null;
    private String query = "";
    private int chosen = 0;
    private int chosenDoc = 0;
    private List<AutocompleteOptions.Entry> opts;
    private List<AutocompleteOptions.Entry> optsWithLocked;

    public static final AutocompleteProvider INSTANCE = new AutocompleteProvider();

    private AutocompleteProvider() {
        optsWithLocked = AutocompleteOptions.INSTANCE.get();
        opts = new ArrayList<>(optsWithLocked);
        opts.removeIf(e -> e.locked());
    }

    public void startPresenting(int mx, int my) {
        presentAt = new Vector2d(mx, my);
    }

    public void stopPresenting() {
        presentAt = null;
        setQuery("");
    }

    public void onCharType(char chr) {
        setQuery(query + chr);
    }

    public boolean onKeyPress(int keyCode, int modifiers, Runnable onClose) {
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
            case GLFW.GLFW_KEY_ESCAPE:
                if (presentAt != null)
                    stopPresenting();
                else
                    onClose.run();
                break;
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_TAB:
                // TODO: accept chosen option
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
                return false;
        }
        return true;
    }

    private void setQuery(String query) {
        this.query = query;
        optsWithLocked = AutocompleteOptions.INSTANCE.get(query);
        opts = new ArrayList<>(optsWithLocked);
        opts.removeIf(AutocompleteOptions.Entry::locked);
        chosen = 0;
        chosenDoc = 0;
    }

    private void offsetChosen(int by) {
        var size = opts.size();
        chosen = ((chosen + by) % size + size) % size;
    }

    private void offsetChosenDoc(int by) {
        var size = opts.get(chosen).impls().size();
        chosenDoc = ((chosenDoc + by) % size + size) % size;
    }

    public void onRender(DrawContext ctx, int mx, int my) {
        if (presentAt == null)
            return;
        renderQueryTooltip(ctx, mx, my);
        if (opts.isEmpty())
            return;
        renderAutocompleteTooltips(ctx, mx, my);
    }

    private void renderQueryTooltip(DrawContext ctx, int mx, int my) {
        var tr = MinecraftClient.getInstance().textRenderer;
        var tInput = !query.equals("")
                ? Text.literal(query)
                        .append(Text.literal(" " + opts.size())
                                .formatted(Formatting.DARK_GRAY))
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, tInput, mx, my);
    }

    private void renderAutocompleteTooltips(DrawContext ctx, int mx, int my) {
        List<Text> options = prepareOptions();
        List<OrderedText> descLines = prepareDescription();
        drawTooltips(ctx, mx, my, options, descLines);
    }

    private List<Text> prepareOptions() {
        var optsStart = Math.max(0, Math.min(chosen - 2, opts.size() - 7));
        var optsEnd = Math.min(opts.size(), optsStart + 7);
        List<Text> options = IntStream.range(optsStart, optsEnd)
                .mapToObj(i -> {
                    var picked = i == chosen;
                    var fmt = picked ? Formatting.BLUE : Formatting.GRAY;
                    var icon = "<" + opts.get(i).dir() + "," + opts.get(i).sig() + "> ";
                    var text = icon + opts.get(i).name();
                    return Text.literal(text).formatted(fmt);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        var lockedN = optsWithLocked.size() - opts.size();
        if (lockedN > 0)
            options.add(Text.translatable("hexcessible.count_locked",
                    lockedN).formatted(Formatting.DARK_GRAY));
        return options;
    }

    private List<OrderedText> prepareDescription() {
        var tr = MinecraftClient.getInstance().textRenderer;
        var opt = opts.get(chosen);
        if (chosenDoc >= opt.impls().size())
            return List.of();

        var docN = "[" + (chosenDoc + 1) + "/" + opt.impls().size() + "]";
        var impl = opt.impls().get(chosenDoc);
        var args = (impl.in() + " -> " + impl.out()).strip();

        var subtext = "\n" + Text.translatable(impl.desc()).getString()
                .replaceAll("\\$\\([^)]*\\)|/\\$", "")
                .replaceAll("[\\s^]_", " ");
        var description = Text.literal(docN + " " + args).formatted(Formatting.GRAY)
                .append(Text.literal(subtext).formatted(Formatting.DARK_GRAY));

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
}
