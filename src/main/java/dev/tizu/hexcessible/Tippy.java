package dev.tizu.hexcessible;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class Tippy {
    private Tippy() {
    }

    public static TextRenderer getTR() {
        return MinecraftClient.getInstance().textRenderer;
    }

    public interface Renderable {
        void render(DrawContext ctx, int x, int y);

        int getHeight();

        int getWidth();
    }

    public static class Tip implements Renderable {
        private static final int MC_TOOLTIP_PADDING = 4;
        private static final int MC_TOOLTIP_OFFSET_X = 8;
        private static final int MC_TOOLTIP_OFFSET_Y = -16;

        private final List<Line> lines = new ArrayList<>();

        public Tip add(Line line) {
            lines.add(line);
            return this;
        }

        private List<Text> getWrappedLines() {
            return this.lines.stream().flatMap(l -> {
                if (l.maxWidth == -1)
                    return List.of(l.text).stream();

                var lines = new ArrayList<Text>();
                var unwrapped = l.text.getString();
                while (!unwrapped.isEmpty()) {
                    var line = getTR().trimToWidth(unwrapped, l.maxWidth);
                    lines.add(Text.literal(line));
                    unwrapped = unwrapped.substring(line.length());
                }
                return lines.stream();
            }).toList();
        }

        public void render(DrawContext ctx, int x, int y) {
            ctx.drawTooltip(getTR(), getWrappedLines(),
                    x - MC_TOOLTIP_OFFSET_X, y - MC_TOOLTIP_OFFSET_Y);
        }

        public int getWidth() {
            return getWrappedLines().stream()
                    .mapToInt(l -> getTR().getWidth(l))
                    .max().orElse(0) + (MC_TOOLTIP_PADDING * 2);
        }

        public int getHeight() {
            var h = getWrappedLines().size() * (getTR().fontHeight + 1)
                    + (MC_TOOLTIP_PADDING * 2);
            if (getWrappedLines().size() == 1)
                h -= 2;
            return h;
        }
    }

    public static Tip tip() {
        return new Tip();
    }

    public static class Line {
        private final Text text;
        int maxWidth = -1;

        public Line(Text text) {
            this.text = text;
        }

        public Line wrapping(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }
    }

    public static Line line(Text text) {
        return new Line(text);
    }

    public static class Stack implements Renderable {
        private final List<Renderable> children = new ArrayList<>();
        private final Direction direction;
        private final Alignment alignment;
        private final int gap;

        public Stack(Direction direction, Alignment alignment, int gap) {
            this.direction = direction;
            this.alignment = alignment;
            this.gap = gap;
        }

        public Stack add(Renderable r) {
            children.add(r);
            return this;
        }

        public void render(DrawContext ctx, int x, int y) {
            var maxW = getWidth();
            var maxH = getHeight();
            var colors = List.of(0xff_0000ff, 0xff_00ff00, 0xff_ff0000, 0xff_00ffff, 0xff_ff00ff);
            var colorIndex = 0;
            for (var r : children) {
                var alignX = (int) ((maxW - r.getWidth()) *
                        (alignment.offset() * direction.dy()));
                var alignY = (int) ((maxH - r.getHeight()) *
                        (alignment.offset() * direction.dx()));

                ctx.drawBorder(x, y, r.getWidth(), r.getHeight(),
                        colors.get(colorIndex % colors.size()));
                colorIndex++;
                ctx.drawBorder(x + alignX, y + alignY, r.getWidth() - alignX,
                        r.getHeight() - alignY, colors.get(colorIndex % colors.size()));
                colorIndex++;

                r.render(ctx, x + alignX, y + alignY);
                x += direction.dx() * (r.getWidth() + gap);
                y += direction.dy() * (r.getHeight() + gap);
            }
        }

        public int getWidth() {
            return children.stream().mapToInt(r -> r.getWidth()).max().orElse(0);
        }

        public int getHeight() {
            return children.stream().mapToInt(r -> r.getHeight()).max().orElse(0);
        }
    }

    public static Stack stack(Direction direction, Alignment alignment, int gap) {
        return new Stack(direction, alignment, gap);
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT;

        public int dx() {
            return switch (this) {
                case UP -> 0;
                case DOWN -> 0;
                case LEFT -> -1;
                case RIGHT -> 1;
            };
        }

        public int dy() {
            return switch (this) {
                case UP -> -1;
                case DOWN -> 1;
                case LEFT -> 0;
                case RIGHT -> 0;
            };
        }
    }

    public enum Alignment {
        START, CENTER, END;

        public float offset() {
            return switch (this) {
                case START -> 0;
                case CENTER -> 0.5f;
                case END -> 1;
            };
        }
    }
}
