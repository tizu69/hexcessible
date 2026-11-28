package dev.tizu.hexcessible.mixin;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.Hexcessible;
import dev.tizu.hexcessible.accessor.CastRef;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor;
import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import dev.tizu.hexcessible.drawstate.DrawState;
import dev.tizu.hexcessible.entries.PatternEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(GuiSpellcasting.class)
public class DrawStateMixin implements DrawStateMixinAccessor {
    @Unique
    private CastRef castref;
    @Unique
    private DrawState state;
    @Unique
    private boolean noActing;

    @Shadow(remap = false)
    private InteractionHand handOpenedWith;
    @Shadow(remap = false)
    private List<ResolvedPattern> patterns;
    @Shadow(remap = false)
    private Set<HexCoord> usedSpots;

    @Shadow(remap = false, prefix = "hexcessible$")
    public boolean hexcessible$drawEnd() {
        return false;
    }

    @Inject(at = @At("HEAD"), method = "init")
    private void init(CallbackInfo info) {
        PatternEntries.INSTANCE.invalidateCaches();
        var castui = (GuiSpellcasting) (Object) this;
        var accessor = new CastingInterfaceAccessor(castui);
        castref = new CastRef(castui, accessor, handOpenedWith, patterns,
                usedSpots, this::hexcessible$drawEnd);
        state = DrawState.getNew(castref);
        noActing = !(Minecraft.getInstance().screen instanceof GuiSpellcasting);
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved")
    private void mouseMoved(double mx, double my, CallbackInfo info) {
        state.onMouseMove(mx, my);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    private void mouseClicked(double mx, double my, int button, CallbackInfoReturnable<Boolean> info) {
        state.onMousePress(mx, my, button);
    }

    @Inject(at = @At("HEAD"), method = "mouseScrolled", cancellable = true)
    private void mouseScrolled(double mx, double my, double deltaX, double deltaY,
            CallbackInfoReturnable<Boolean> info) {
        if (state.onMouseScroll((int) deltaY))
            info.setReturnValue(true);
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta,
                       CallbackInfo info) {
        if (!noActing && DrawState.shouldClose(state)) {
            ((GuiSpellcasting) (Object) this).closeForReal();
            return;
        }

        var nextState = DrawState.updateRequired((GuiSpellcasting) (Object) this, state);
        if (nextState != null)
            state = nextState;

        if (Hexcessible.cfg().debug) {
            renderDebug(ctx, state.getClass().getSimpleName(), 0);
            var debug = state.getDebugInfo();
            for (int i = 0; i < debug.size(); i++)
                renderDebug(ctx, debug.get(i), i + 1);
        }

        if (!noActing) {
            state.onRender(ctx, mouseX, mouseY);
            renderHints(ctx);
        }
    }

    @Unique
    private void renderDebug(GuiGraphics ctx, String text, int i) {
        ctx.drawString(Minecraft.getInstance().font,
                text, 5, 5 + (i * 10), 0xFFFFFF);
    }

    @Unique
    private void renderHints(GuiGraphics ctx) {
        if (!Hexcessible.cfg().shortcutHints)
            return;
        var hints = state.getHints();
        if (hints.isEmpty())
            return;

        var x = 6;
        var y = ctx.guiHeight() - 16;
        for (var hint : hints.entrySet()) {
            var text = Component.empty()
                    .append(Component.literal(hint.getKey() + " ")
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("hexcessible.hint." + hint.getValue())
                            .withStyle(ChatFormatting.GRAY));
            ctx.drawString(Minecraft.getInstance().font,
                    text, x, y, 0xFFFFFF);
            y -= 10;
        }
    }

    @Inject(method = "drawStart", at = @At("HEAD"), remap = false, cancellable = true)
    private void drawStart(double mxOut, double myOut, CallbackInfoReturnable<Boolean> cir) {
        if (!state.allowStartDrawing())
            cir.setReturnValue(false);
    }

    @Override
    public DrawState state() {
        return state;
    }

    @Override
    public @Nullable HexPattern getPatternAt(int x, int y) {
        var coord = ((GuiSpellcasting) (Object) this).pxToCoord(new Vec2(x, y));
        return patterns.stream()
                .filter(p -> p.getOrigin().equals(coord)
                        || p.getPattern().positions().stream()
                                .map(pt -> pt.plus(p.getOrigin()))
                                .anyMatch(pt -> pt.equals(coord)))
                .findFirst()
                .map(ResolvedPattern::getPattern)
                .orElse(null);
    }

    @Override
    public void disallowTyping() {
        castref.disallowTyping();
    }
}