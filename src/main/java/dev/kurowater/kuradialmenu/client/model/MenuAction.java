package dev.kurowater.kuradialmenu.client.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

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
            if (client.player == null) {
                return;
            }

            // キーの押下をシミュレート
            // timesPressed を増加させることで、即座に実行されるアクション（インベントリ開閉など）に対応
            KeyBindingAccessor accessor = (KeyBindingAccessor) keyBinding;
            accessor.kuradialmenu$incrementTimesPressed();
        }

        @Override
        public String getId() {
            return keyBinding.getTranslationKey();
        }

        @Override
        public String getDisplayName() {
            return keyBinding.getBoundKeyLocalizedText().getString();
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
        void kuradialmenu$incrementTimesPressed();
    }
}

