package dev.tizu.hexcessible.accessor;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.tizu.hexcessible.drawstate.DrawState;

public interface DrawStateMixinAccessor {
    DrawState state();

    @Nullable
    HexPattern getPatternAt(int x, int y);

    void disallowTyping();

    boolean hexcessible$drawEnd();
}