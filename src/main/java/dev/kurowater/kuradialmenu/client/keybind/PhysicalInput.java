package dev.kurowater.kuradialmenu.client.keybind;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Reads physical input without consuming KeyBinding's queued presses. */
public final class PhysicalInput {
    private PhysicalInput() { }

    public static boolean isDown(MinecraftClient client, KeyBinding binding) {
        if (!client.isWindowFocused() || binding.isUnbound()) {
            return false;
        }
        InputUtil.Key key = InputUtil.fromTranslationKey(binding.getBoundKeyTranslationKey());
        long window = client.getWindow().getHandle();
        int code = key.getCode();
        if (key.getCategory() == InputUtil.Type.MOUSE) {
            return code >= GLFW.GLFW_MOUSE_BUTTON_1 && code <= GLFW.GLFW_MOUSE_BUTTON_LAST
                    && GLFW.glfwGetMouseButton(window, code) == GLFW.GLFW_PRESS;
        }
        if (key.getCategory() == InputUtil.Type.KEYSYM) {
            return code >= GLFW.GLFW_KEY_SPACE && code <= GLFW.GLFW_KEY_LAST
                    && InputUtil.isKeyPressed(window, code);
        }
        // GLFW polls key symbols, not platform scan codes. Resolve only this uncommon binding type.
        for (int symbol = GLFW.GLFW_KEY_SPACE; symbol <= GLFW.GLFW_KEY_LAST; symbol++) {
            if (isValidSymbol(symbol) && GLFW.glfwGetKeyScancode(symbol) == code
                    && InputUtil.isKeyPressed(window, symbol)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidSymbol(int key) {
        // GLFW's key enum has gaps: querying those values generates GLFW_INVALID_ENUM.
        return key == 32 || key == 39 || (key >= 44 && key <= 57) || key == 59 || key == 61
                || (key >= 65 && key <= 93) || key == 96 || (key >= 161 && key <= 162)
                || (key >= 256 && key <= 269) || (key >= 280 && key <= 284)
                || (key >= 290 && key <= 314) || (key >= 320 && key <= 336)
                || (key >= 340 && key <= 348);
    }
}
