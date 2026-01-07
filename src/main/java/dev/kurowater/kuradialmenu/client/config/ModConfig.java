package dev.kurowater.kuradialmenu.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Mod の設定データクラス
 */
public class ModConfig {

    // ==================== 一般設定 ====================

    /**
     * ラジアルメニューのスロット数 (4, 8, 12 のいずれか)
     */
    public int slotCount = 8;

    // ==================== UI 設定 ====================

    /**
     * メニューの内側半径 (ピクセル)
     */
    public int innerRadius = 30;

    /**
     * メニューの外側半径 (ピクセル)
     */
    public int outerRadius = 100;

    /**
     * 中央のデッドゾーン半径 (この範囲内ではスロット選択なし)
     */
    public int centerDeadzone = 20;

    /**
     * メニューの背景色 (ARGB)
     */
    public int backgroundColor = 0x80000000;

    /**
     * 選択中スロットのハイライト色 (ARGB)
     */
    public int highlightColor = 0x80FFFFFF;

    /**
     * スロットの境界線色 (ARGB)
     */
    public int borderColor = 0xFFFFFFFF;

    // ==================== スロット設定 ====================

    /**
     * 各スロットに割り当てられたアクション (キーバインドID) のリスト
     */
    public List<String> slotActions = new ArrayList<>();

    /**
     * デフォルト設定で初期化
     */
    public ModConfig() {
        initializeDefaultSlots();
    }

    /**
     * デフォルトのスロット設定を初期化
     */
    private void initializeDefaultSlots() {
        slotActions.clear();
        // 8スロットのデフォルト設定
        slotActions.add("key.inventory");           // 上: インベントリ
        slotActions.add("key.swapOffhand");         // 右上: オフハンド切替
        slotActions.add("key.drop");                // 右: ドロップ
        slotActions.add("key.togglePerspective");   // 右下: 視点切替
        slotActions.add("key.command");             // 下: コマンド
        slotActions.add("key.screenshot");          // 左下: スクリーンショット
        slotActions.add("key.advancements");        // 左: 進捗
        slotActions.add("key.playerlist");          // 左上: プレイヤーリスト
    }

    /**
     * スロット数に合わせてスロットアクションリストのサイズを調整
     */
    public void adjustSlotActionsSize() {
        while (slotActions.size() < slotCount) {
            slotActions.add("");
        }
        while (slotActions.size() > slotCount) {
            slotActions.remove(slotActions.size() - 1);
        }
    }

    /**
     * 設定の妥当性を検証し必要に応じて修正
     */
    public void validate() {
        // スロット数の検証 (4, 8, 12 のみ許可)
        if (slotCount != 4 && slotCount != 8 && slotCount != 12) {
            slotCount = 8;
        }

        // 半径の検証
        innerRadius = Math.max(10, Math.min(100, innerRadius));
        outerRadius = Math.max(50, Math.min(300, outerRadius));
        centerDeadzone = Math.max(5, Math.min(50, centerDeadzone));

        // 外側半径が内側半径より大きいことを保証
        if (outerRadius <= innerRadius) {
            outerRadius = innerRadius + 50;
        }

        // スロットアクションのサイズ調整
        adjustSlotActionsSize();
    }

    /**
     * デフォルト設定にリセット
     */
    public void resetToDefault() {
        slotCount = 8;
        innerRadius = 30;
        outerRadius = 100;
        centerDeadzone = 20;
        backgroundColor = 0x80000000;
        highlightColor = 0x80FFFFFF;
        borderColor = 0xFFFFFFFF;
        initializeDefaultSlots();
    }
}

