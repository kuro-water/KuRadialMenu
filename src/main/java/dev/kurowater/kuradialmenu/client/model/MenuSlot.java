package dev.kurowater.kuradialmenu.client.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * ラジアルメニューの個別スロットを表すクラス
 */
public class MenuSlot {

    private final int index;
    private final String actionId;

    /**
     * @param index スロットインデックス
     * @param actionId アクションID (キーバインドの翻訳キー)
     */
    public MenuSlot(int index, String actionId) {
        this.index = index;
        this.actionId = actionId;
    }

    /**
     * スロットインデックスを取得
     */
    public int getIndex() {
        return index;
    }

    /**
     * アクションIDを取得
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * このスロットに有効なアクションが設定されているか
     */
    public boolean hasAction() {
        return actionId != null && !actionId.isEmpty();
    }

    /**
     * 関連付けられたキーバインドを取得
     *
     * @return キーバインド (見つからない場合は null)
     */
    @Nullable
    public KeyBinding getKeyBinding() {
        if (!hasAction()) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        for (KeyBinding keyBinding : client.options.allKeys) {
            if (keyBinding.getTranslationKey().equals(actionId)) {
                return keyBinding;
            }
        }
        return null;
    }

    /**
     * 表示用のラベルテキストを取得
     *
     * @return ラベルテキスト
     */
    public Text getLabel() {
        if (!hasAction()) {
            return Text.literal("");
        }

        // キーバインドの翻訳キーから表示名を取得
        return Text.translatable(actionId);
    }

    /**
     * 表示用の短いラベルテキストを取得 (アイコン代わり)
     *
     * @return 短いラベル
     */
    public String getShortLabel() {
        KeyBinding keyBinding = getKeyBinding();
        if (keyBinding == null) {
            return "";
        }

        // キーの名前を短く表示
        String boundKeyName = keyBinding.getBoundKeyLocalizedText().getString();
        if (boundKeyName.length() > 3) {
            return boundKeyName.substring(0, 3);
        }
        return boundKeyName;
    }
}

