package dev.tizu.hexcessible.drawstate;

import dev.tizu.hexcessible.accessor.CastRef;

public final class Idling extends DrawState {
    public Idling(CastRef castref) {
        super(castref);
    }

    @Override
    public void requestExit() {
        wantsExit = true;
    }

    @Override
    public void onCharType(char chr) {
        if (KeyboardDrawing.validSig.contains(chr))
            nextState = new KeyboardDrawing(castref, String.valueOf(chr));
    }
}
