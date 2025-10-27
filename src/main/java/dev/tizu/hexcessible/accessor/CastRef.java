package dev.tizu.hexcessible.accessor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.math.HexAngle;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.msgs.MsgNewSpellPatternC2S;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec2f;

public class CastRef {
    private final GuiSpellcasting castui;
    private final Hand handOpenedWith;
    private final List<ResolvedPattern> patterns;
    private final Set<HexCoord> usedSpots;

    public CastRef(GuiSpellcasting castui, Hand handOpenedWith,
            List<ResolvedPattern> patterns, Set<HexCoord> usedSpots) {
        this.castui = castui;
        this.handOpenedWith = handOpenedWith;
        this.patterns = patterns;
        this.usedSpots = usedSpots;
    }

    public HexCoord pxToCoord(Vec2f px) {
        return castui.pxToCoord(px);
    }

    public Vec2f coordToPx(HexCoord coord) {
        return castui.coordToPx(coord);
    }

    public float hexSize() {
        return castui.hexSize();
    }

    public boolean isUsed(HexCoord coord) {
        return usedSpots.contains(coord) || !isVisible(coord);
    }

    public boolean isVisible(HexCoord coord) {
        var pos = coordToPx(coord);
        return pos.x >= 0 && pos.x < castui.width
                && pos.y >= 0 && pos.y < castui.height;
    }

    public void execute(HexPattern pat, HexCoord start) {
        this.patterns.add(new ResolvedPattern(pat, start,
                ResolvedPatternType.UNRESOLVED));
        this.usedSpots.addAll(pat.positions(start));
        IClientXplatAbstractions.INSTANCE.sendPacketToServer(
                new MsgNewSpellPatternC2S(handOpenedWith, pat, patterns));
    }

    public static record PatternPlacement(HexCoord coord, HexDir startDir) {
    }

    /**
     * Finds the closest available spot where the pattern can be drawn,
     * trying all possible rotations and preferring the one closest to start.
     * Returns both the coordinate and the starting direction needed.
     */
    @Nullable
    public PatternPlacement findClosestAvailable(HexCoord start, HexPattern pat) {
        Queue<HexCoord> queue = new LinkedList<>();
        Set<HexCoord> visited = new HashSet<>();
        queue.add(start);
        visited.add(start); // so that start doesn't get re-queued

        while (!queue.isEmpty()) {
            HexCoord current = queue.poll();
            for (HexDir startDir : HexDir.values())
                if (fits(current, pat, startDir))
                    return new PatternPlacement(current, startDir);
            for (HexDir dir : HexDir.values()) {
                HexCoord next = current.plus(dir);
                if (visited.add(next))
                    queue.add(next);
            }
            if (visited.size() > 512)
                return null; // breakout
        }
        return null;
    }

    /**
     * Checks if a pattern can be drawn starting from the origin coordinate
     * with the given starting direction, without overlapping any used spots.
     */
    private boolean fits(HexCoord origin, HexPattern pat, HexDir startDir) {
        if (isUsed(origin))
            return false;

        HexCoord current = origin;
        HexDir dir = startDir;

        current = current.plus(dir);
        if (isUsed(current))
            return false;

        for (HexAngle angle : pat.getAngles()) {
            dir = dir.rotatedBy(angle);
            current = current.plus(dir);
            if (isUsed(current))
                return false;
        }

        return true;
    }
}