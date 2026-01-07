package dev.kurowater.kuradialmenu.client.keybind;

import dev.kurowater.kuradialmenu.KuRadialMenuClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Mod のキーバインドを登録・管理するクラス
 */
public final class ModKeybinds {

    public static final String CATEGORY = "key.category.kuradialmenu";

    /**
     * ラジアルメニューを開くキー (デフォルト: R)
     */
    public static final KeyBinding OPEN_MENU = new KeyBinding(
            "key.kuradialmenu.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    private ModKeybinds() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * キーバインドを登録
     */
    public static void register() {
        KeyBindingHelper.registerKeyBinding(OPEN_MENU);
        KuRadialMenuClient.LOGGER.info("Keybinds registered");
    }
}

