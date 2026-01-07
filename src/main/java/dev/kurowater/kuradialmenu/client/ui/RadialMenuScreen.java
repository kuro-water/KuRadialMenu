package dev.kurowater.kuradialmenu.client.ui;

import dev.kurowater.kuradialmenu.client.model.MenuSlot;
import dev.kurowater.kuradialmenu.client.model.RadialMenu;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

/**
 * ラジアルメニューの Screen 実装
 */
public class RadialMenuScreen extends Screen {

    private final RadialMenu menu;
    private final RadialMenuRenderer renderer;

    public RadialMenuScreen() {
        super(Text.translatable("screen.kuradialmenu.radial_menu"));
        this.menu = new RadialMenu();
        this.renderer = new RadialMenuRenderer();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        super.tick();
    }

    //? if >=1.21 {
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1.21以降: 背景ぼかしを完全に無効化（空実装）
        // super.renderBackground() を呼ばないことで背景描画とぼかし処理をスキップ
    }
    //?}

    //? if >=1.20 {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // マウス位置に基づいて選択を更新
        menu.updateSelection(centerX, centerY, mouseX, mouseY);

        // メニューを描画
        renderer.render(context, menu, centerX, centerY);

        // ツールチップを描画 (選択中のスロットがある場合)
        menu.getSelectedSlot().ifPresent(slot -> {
            if (slot.hasAction()) {
                context.drawTooltip(this.textRenderer, slot.getLabel(), mouseX, mouseY);
            }
        });

        super.render(context, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // マウス位置に基づいて選択を更新
        menu.updateSelection(centerX, centerY, mouseX, mouseY);

        // メニューを描画
        renderer.render(matrices, menu, centerX, centerY);

        // ツールチップを描画 (選択中のスロットがある場合)
        menu.getSelectedSlot().ifPresent(slot -> {
            if (slot.hasAction()) {
                renderTooltip(matrices, slot.getLabel(), mouseX, mouseY);
            }
        });

        super.render(matrices, mouseX, mouseY, delta);
    }
    *///?}

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    /**
     * 選択中のアクションを実行
     */
    public void executeSelectedAction() {
        menu.executeSelectedAction();
    }

    /**
     * 現在選択中のスロットを取得
     */
    public java.util.Optional<MenuSlot> getSelectedSlot() {
        return menu.getSelectedSlot();
    }
}

