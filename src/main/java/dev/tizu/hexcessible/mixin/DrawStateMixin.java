package dev.tizu.hexcessible.mixin;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.tizu.hexcessible.HexcessibleConfig;
import dev.tizu.hexcessible.accessor.CastingInterfaceAccessor;
import dev.tizu.hexcessible.accessor.DrawStateMixinAccessor;
import dev.tizu.hexcessible.drawstate.DrawState;
import dev.tizu.hexcessible.drawstate.DrawState.CastRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Hand;

@Mixin(GuiSpellcasting.class)
public class DrawStateMixin implements DrawStateMixinAccessor {
    @Unique
    private CastingInterfaceAccessor accessor;
    @Unique
    private DrawState state;

    @Shadow(remap = false)
    private Hand handOpenedWith;
    @Shadow(remap = false)
    private List<ResolvedPattern> patterns;
    @Shadow(remap = false)
    private Set<HexCoord> usedSpots;

    @Inject(at = @At("HEAD"), method = "init")
    private void init(CallbackInfo info) {
        var castui = (GuiSpellcasting) (Object) this;
        accessor = new CastingInterfaceAccessor(castui);
        var castref = new CastRef(castui, handOpenedWith, patterns, usedSpots);
        state = DrawState.getNew(castref);
    }

    @Inject(at = @At("HEAD"), method = "mouseMoved")
    private void mouseMoved(double mx, double my, CallbackInfo info) {
        state.onMouseMove(mx, my);
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    private void mouseClicked(double mx, double my, int button, CallbackInfoReturnable<Boolean> info) {
        state.onMousePress(mx, my, button);
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta,
            CallbackInfo info) {
        if (DrawState.shouldClose(state)) {
            ((GuiSpellcasting) (Object) this).close();
            return;
        }

        var nextState = DrawState.updateRequired((GuiSpellcasting) (Object) this, state);
        if (nextState != null)
            state = nextState;

        if (HexcessibleConfig.get().debug) {
            renderDebug(ctx, state.getClass().getSimpleName(), 0);
            var debug = state.getDebugInfo();
            for (int i = 0; i < debug.size(); i++)
                renderDebug(ctx, debug.get(i), i + 1);
        }

        state.onRender(ctx, mouseX, mouseY);
    }

    @Unique
    private void renderDebug(DrawContext ctx, String text, int i) {
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                text, 5, 5 + (i * 10), 0xFFFFFF);
    }

    @WrapMethod(method = "drawStart", remap = false)
    private boolean drawStart(double mxOut, double myOut, Operation<Boolean> original) {
        return state.allowStartDrawing() && original.call(mxOut, myOut);
    }

    @Override
    public DrawState state() {
        return state;
    }
}