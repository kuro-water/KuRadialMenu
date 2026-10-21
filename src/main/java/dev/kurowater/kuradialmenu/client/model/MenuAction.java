package dev.kurowater.kuradialmenu.client.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import dev.kurowater.kuradialmenu.client.keybind.ActionDispatcher;

/**
 * ラジアルメニューのアクションを表すインターフェース
 */
public interface MenuAction {

    /**
     * アクションを実行
     *
     * @param client MinecraftClient インスタンス
     */
    void execute(MinecraftClient client);

    /**
     * アクションの識別子を取得
     *
     * @return アクションID
     */
    String getId();

    /**
     * アクションの表示名を取得
     *
     * @return 表示名
     */
    String getDisplayName();

    /**
     * キーバインドベースのアクション実装
     */
    class KeyBindingAction implements MenuAction {

        private final KeyBinding keyBinding;

        public KeyBindingAction(KeyBinding keyBinding) {
            this.keyBinding = keyBinding;
        }

        @Override
        public void execute(MinecraftClient client) {
            ActionDispatcher.execute(client, keyBinding);
        }

        @Override
        public String getId() {
            return keyBinding.getTranslationKey();
        }

        @Override
        public String getDisplayName() {
            return Text.translatable(keyBinding.getTranslationKey()).getString();
        }

        /**
         * 対象のキーバインドを取得
         */
        public KeyBinding getKeyBinding() {
            return keyBinding;
        }
    }

    /**
     * KeyBinding の内部状態にアクセスするためのインターフェース
     * Mixin で実装される
     */
    interface KeyBindingAccessor {
        void kuradialmenu$queuePress();
        void kuradialmenu$setPressed(boolean pressed);
        void kuradialmenu$clearQueuedPress();
    }
}

