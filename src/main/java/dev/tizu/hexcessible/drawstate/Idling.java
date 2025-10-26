package dev.tizu.hexcessible.drawstate;

public final class Idling extends DrawState {
    @Override
    public void requestExit() {
        wantsExit = true;
    }

    @Override
    public void onCharType(char chr) {
        if (KeyboardDrawing.validSig.contains(chr))
            nextState = new KeyboardDrawing(String.valueOf(chr));
    }
}
