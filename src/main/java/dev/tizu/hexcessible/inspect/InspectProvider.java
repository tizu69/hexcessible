package dev.tizu.hexcessible.inspect;

import java.util.ArrayList;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.tizu.hexcessible.CastingInterfaceAccessor;
import dev.tizu.hexcessible.PatternEntries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InspectProvider {
    private final CastingInterfaceAccessor castui;
    private long lastMouseMoved = 0;

    public InspectProvider(CastingInterfaceAccessor castui) {
        this.castui = castui;
    }

    public void onMouseMove() {
        lastMouseMoved = System.currentTimeMillis();
    }

    public void onMouseRender(DrawContext ctx, int mx, int my) {
        if (System.currentTimeMillis() - lastMouseMoved < 500
                || !castui.isDrawing())
            return;
        var state = new State(castui.getStart(), castui.getPattern());
        InspectProvider.render(ctx, mx, my, state);
    }

    private static void render(DrawContext ctx, int mx, int my, State state) {
        var tr = MinecraftClient.getInstance().textRenderer;

        var pattern = state.pattern().anglesSignature();
        if (pattern.isEmpty())
            return;

        ctx.drawTooltip(tr, Text.literal(pattern), mx, my);

        var entry = PatternEntries.INSTANCE.getFromSig(pattern);
        if (entry == null)
            return;
        var subtext = new ArrayList<Text>();
        subtext.add(Text.literal(entry.toString()).formatted(Formatting.BLUE));
        for (var impl : entry.impls())
            subtext.add(Text.literal(impl.getArgs()).formatted(Formatting.DARK_GRAY));
        ctx.drawTooltip(tr, subtext, mx, my + 17);
    }

    public record State(HexCoord start, HexPattern pattern) {
    }
}
