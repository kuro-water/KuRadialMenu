package dev.kurowater.kuradialmenu.client.keybind;

import dev.kurowater.kuradialmenu.KuRadialMenuClient;
import dev.kurowater.kuradialmenu.client.config.ModConfigScreen;
import dev.kurowater.kuradialmenu.client.ui.RadialMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

/**
 * キーバインドの状態を監視し、メニューの開閉を制御するハンドラー
 */
public final class KeybindHandler {

    private static boolean wasKeyPressed = false;

    private KeybindHandler() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * キーバインドハンドラーを登録
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::onClientTick);
    }

    /**
     * キーバインドが現在押されているかどうかを判定
     * <p>
     * KeyBinding.isPressed() はキュー消費型のため長押し検出には不適切。
     * 代わりに InputUtil.isKeyPressed() でキーの物理的な押下状態を直接確認する。
     * getBoundKeyTranslationKey() を使用することで、ユーザーがキー設定を変更した場合にも追従する。
     * </p>
     *
     * @param client MinecraftClient インスタンス
     * @return キーが押されている場合 true
     */
    private static boolean isMenuKeyPressed(MinecraftClient client) {
        long handle = client.getWindow().getHandle();
        InputUtil.Key boundKey = InputUtil.fromTranslationKey(
                ModKeybinds.OPEN_MENU.getBoundKeyTranslationKey()
        );
        return InputUtil.isKeyPressed(handle, boundKey.getCode());
    }

    /**
     * クライアントティック毎に呼び出される
     */
    private static void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        // 設定画面を開くキーの処理（wasPressed は1回だけ消費される）
        if (ModKeybinds.OPEN_CONFIG.wasPressed()) {
            KuRadialMenuClient.LOGGER.debug("Config key pressed");
            if (client.currentScreen == null) {
                client.setScreen(ModConfigScreen.create(null));
            }
        }

        boolean isKeyPressed = isMenuKeyPressed(client);

        // キーが押された瞬間にメニューを開く
        if (isKeyPressed && !wasKeyPressed) {
            KuRadialMenuClient.LOGGER.debug("Menu key pressed");
            if (client.currentScreen == null) {
                client.setScreen(new RadialMenuScreen());
            }
        }

        // キーが離された瞬間にメニューを閉じる (メニュー画面表示中の場合)
        if (!isKeyPressed && wasKeyPressed) {
            KuRadialMenuClient.LOGGER.debug("Menu key released");
            if (client.currentScreen instanceof RadialMenuScreen menuScreen) {
                menuScreen.executeSelectedAction();
                client.setScreen(null);
            }
        }

        wasKeyPressed = isKeyPressed;
    }
}

