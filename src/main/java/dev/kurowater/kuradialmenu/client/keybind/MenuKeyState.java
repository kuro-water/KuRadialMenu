package dev.kurowater.kuradialmenu.client.keybind;

/** Edge detector which requires a release after focus loss or leaving a world. */
public final class MenuKeyState {
    public enum Change { NONE, PRESSED, RELEASED }

    private boolean pressed;
    private boolean blocked;

    public Change update(boolean down, boolean available) {
        if (!available) {
            pressed = false;
            blocked = true;
            return Change.NONE;
        }
        if (blocked) {
            blocked = down;
            return Change.NONE;
        }
        if (down == pressed) {
            return Change.NONE;
        }
        pressed = down;
        return down ? Change.PRESSED : Change.RELEASED;
    }
}
