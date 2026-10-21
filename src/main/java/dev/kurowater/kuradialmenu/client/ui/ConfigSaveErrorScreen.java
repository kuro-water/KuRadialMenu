package dev.kurowater.kuradialmenu.client.ui;

import net.minecraft.client.gui.screen.Screen;
import dev.kurowater.kuradialmenu.client.config.ConfigHandler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

/** Shown after YACL finishes closing, so a failed disk write cannot look like a successful save. */
public final class ConfigSaveErrorScreen extends Screen {
    private final Screen parent;
    private Text detail;

    public ConfigSaveErrorScreen(Screen parent, String detail) {
        super(Text.translatable("config.kuradialmenu.save_error.title"));
        this.parent = parent;
        this.detail = Text.translatable("config.kuradialmenu.save_error.detail", detail == null ? "" : detail);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("config.kuradialmenu.save_error.retry"), button -> {
            if (ConfigHandler.trySave()) {
                close();
            } else {
                detail = Text.translatable("config.kuradialmenu.save_error.detail", ConfigHandler.getLastError());
            }
        }).dimensions(width / 2 - 154, height - 30, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(width / 2 + 4, height - 30, 150, 20).build());
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    //? if >=1.20 {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xF0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFF7777);
        int y = 45;
        for (OrderedText line : textRenderer.wrapLines(detail, Math.max(100, width - 40))) {
            context.drawTextWithShadow(textRenderer, line, 20, y, 0xFFFFFF);
            y += 12;
        }
        super.render(context, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fill(matrices, 0, 0, width, height, 0xF0101010);
        drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, 20, 0xFF7777);
        int y = 45;
        for (OrderedText line : textRenderer.wrapLines(detail, Math.max(100, width - 40))) {
            textRenderer.drawWithShadow(matrices, line, 20, y, 0xFFFFFF);
            y += 12;
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
    *///?}
}
