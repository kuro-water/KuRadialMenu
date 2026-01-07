package dev.kurowater.kuradialmenu.client.model;

import dev.kurowater.kuradialmenu.client.config.ConfigHandler;
import dev.kurowater.kuradialmenu.client.config.ModConfig;
import dev.kurowater.kuradialmenu.client.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ラジアルメニューのデータモデル
 */
public class RadialMenu {

    private final List<MenuSlot> slots;
    private int selectedIndex = -1;

    public RadialMenu() {
        this.slots = new ArrayList<>();
        initializeSlots();
    }

    /**
     * 設定に基づいてスロットを初期化
     */
    private void initializeSlots() {
        slots.clear();
        ModConfig config = ConfigHandler.get();

        for (int i = 0; i < config.slotCount; i++) {
            String actionId = i < config.slotActions.size() ? config.slotActions.get(i) : "";
            slots.add(new MenuSlot(i, actionId));
        }
    }

    /**
     * マウス位置に基づいて選択中のスロットを更新
     *
     * @param centerX メニュー中心X座標
     * @param centerY メニュー中心Y座標
     * @param mouseX マウスX座標
     * @param mouseY マウスY座標
     */
    public void updateSelection(double centerX, double centerY, double mouseX, double mouseY) {
        ModConfig config = ConfigHandler.get();
        double distance = MathUtil.getDistance(centerX, centerY, mouseX, mouseY);

        // デッドゾーン内では選択なし
        if (distance < config.centerDeadzone) {
            selectedIndex = -1;
            return;
        }

        double angle = MathUtil.getAngle(centerX, centerY, mouseX, mouseY);
        selectedIndex = MathUtil.getSlotIndex(angle, config.slotCount);
    }

    /**
     * 選択中のスロットを取得
     *
     * @return 選択中のスロット (選択なしの場合は empty)
     */
    public Optional<MenuSlot> getSelectedSlot() {
        if (selectedIndex < 0 || selectedIndex >= slots.size()) {
            return Optional.empty();
        }
        return Optional.of(slots.get(selectedIndex));
    }

    /**
     * 選択中のスロットのインデックスを取得
     *
     * @return スロットインデックス (-1 は選択なし)
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * 全スロットを取得
     *
     * @return スロットリスト
     */
    public List<MenuSlot> getSlots() {
        return slots;
    }

    /**
     * スロット数を取得
     *
     * @return スロット数
     */
    public int getSlotCount() {
        return slots.size();
    }

    /**
     * 選択中のアクションを実行
     */
    public void executeSelectedAction() {
        getSelectedSlot().ifPresent(slot -> {
            KeyBinding keyBinding = slot.getKeyBinding();
            if (keyBinding != null) {
                // キーバインドを一時的に押下状態にする
                simulateKeyPress(keyBinding);
            }
        });
    }

    /**
     * キーバインドの押下をシミュレート
     */
    private void simulateKeyPress(@Nullable KeyBinding keyBinding) {
        if (keyBinding == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // キーバインドの押下状態を一時的に true にして、次の tick で実行される
        keyBinding.setPressed(true);

        // 一部のキーバインドは押下フラグだけでなく、時間経過も必要なため
        // 即座に false に戻さず、次のフレームで自然に解除されるようにする
    }
}

