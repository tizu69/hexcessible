package dev.tizu.hexcessible.inspect;

import java.util.function.Function;

import org.joml.Vector4i;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.tizu.hexcessible.Hexcessible;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

class InspectRenderer {
    private InspectRenderer() {
    }

    public static void render(DrawContext ctx, int mx, int my, State state) {
        var tr = MinecraftClient.getInstance().textRenderer;

        var pattern = state.pattern().anglesSignature();
        if (pattern.isEmpty())
            return;

        ctx.drawTooltip(tr, Text.literal(pattern), mx, my);
    }

    public record State(HexCoord start, HexPattern pattern) {
    }
}
