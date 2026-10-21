package dev.kurowater.kuradialmenu.client.keybind;

import dev.kurowater.kuradialmenu.client.keybind.MenuKeyState.Change;

/** No game or test framework required: run with java after compilation. */
public final class MenuKeyStateTest {
    public static void main(String[] args) {
        MenuKeyState state = new MenuKeyState();
        expect(Change.NONE, state.update(false, true));
        expect(Change.PRESSED, state.update(true, true));
        expect(Change.NONE, state.update(true, true));
        expect(Change.RELEASED, state.update(false, true));
        expect(Change.NONE, state.update(false, true));

        // Losing focus with a highlighted item must cancel, not execute it.
        expect(Change.PRESSED, state.update(true, true));
        expect(Change.NONE, state.update(true, false));
        expect(Change.NONE, state.update(true, true));
        expect(Change.NONE, state.update(false, true));
        expect(Change.PRESSED, state.update(true, true));
        expect(Change.RELEASED, state.update(false, true));

        // Joining a world while holding the menu key requires release before opening.
        state = new MenuKeyState();
        expect(Change.NONE, state.update(false, false));
        expect(Change.NONE, state.update(true, true));
        expect(Change.NONE, state.update(true, true));
        expect(Change.NONE, state.update(false, true));
        expect(Change.PRESSED, state.update(true, true));

        // Esc closes the UI independently; a held key must not generate a second opening.
        expect(Change.NONE, state.update(true, true));
        expect(Change.NONE, state.update(true, true));
        expect(Change.RELEASED, state.update(false, true));
    }

    private static void expect(Change expected, Change actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
