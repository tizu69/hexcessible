package dev.tizu.hexcessible;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

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

    public static final AutocompleteProvider INSTANCE = new AutocompleteProvider();

    private AutocompleteProvider() {
        opts = AutocompleteOptions.INSTANCE.get();
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
        opts = AutocompleteOptions.INSTANCE.get(query);
        chosen = 0;
        chosenDoc = 0;
    }

    private void offsetChosen(int by) {
        var size = AutocompleteOptions.INSTANCE.get(query).size();
        chosen = ((chosen + by) % size + size) % size;
    }

    private void offsetChosenDoc(int by) {
        var size = 4;
        chosenDoc = ((chosenDoc + by) % size + size) % size;
    }

    public void onRender(DrawContext ctx, int mx, int my) {
        if (presentAt == null)
            return;
        var tr = MinecraftClient.getInstance().textRenderer;

        var tInput = !query.equals("")
                ? Text.literal(query)
                        .append(Text.literal(" " + opts.size())
                                .formatted(Formatting.DARK_GRAY))
                : Text.translatable("hexcessible.start_typing")
                        .formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
        ctx.drawTooltip(tr, tInput, mx, my);

        if (opts.isEmpty())
            return;
        var optsStart = Math.max(0, Math.min(chosen - 2, opts.size() - 7));
        var optsEnd = Math.min(opts.size(), optsStart + 7);
        List<Text> options = IntStream.range(optsStart, optsEnd)
                .mapToObj(i -> {
                    var picked = i == chosen;
                    var fmt = picked ? Formatting.BLUE : Formatting.GRAY;
                    var text = "<" + opts.get(i).dir() + "," + opts.get(i).sig() + "> "
                            + opts.get(i).name();
                    return Text.literal(text).formatted(fmt);
                })
                .collect(Collectors.toList());

        var desc = "Read the iota associated with the given pattern out of the Akashic Library with its Record at the given position. This has no range limit. Costs about one Amethyst Dust.";
        var description = Text.literal("vector, pattern -> any").formatted(Formatting.GRAY)
                .append(Text.literal("\n" + desc).formatted(Formatting.DARK_GRAY));
        List<OrderedText> descLines = tr.wrapLines(description, 170);

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

        var descriptionY = renderAbove ? my - (descLines.size() * fontH) - 9 : my + 17;
        var descriptionX = descLeft ? optionsX - descW - 9 : optionsX + optsW + 9;
        ctx.drawTooltip(tr, descLines, HoveredTooltipPositioner.INSTANCE, descriptionX, descriptionY);
    }
}
