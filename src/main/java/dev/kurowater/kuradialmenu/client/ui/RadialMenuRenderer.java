package dev.kurowater.kuradialmenu.client.ui;

import dev.kurowater.kuradialmenu.client.config.ConfigHandler;
import dev.kurowater.kuradialmenu.client.config.ModConfig;
import dev.kurowater.kuradialmenu.client.model.MenuSlot;
import dev.kurowater.kuradialmenu.client.model.RadialMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.GameRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >=1.20 {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferRenderer;
//? if >=1.21 {
import org.joml.Matrix4f;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
import org.joml.Matrix4f;
*///?}
//?} else {
/*import net.minecraft.client.util.math.MatrixStack;
*///?}

/**
 * ラジアルメニューの描画を担当するクラス
 */
public class RadialMenuRenderer {

    private static final int SEGMENTS_PER_SLOT = 32;

    public RadialMenuRenderer() {
    }

    //? if >=1.20 {
    public void render(DrawContext context, RadialMenu menu, int centerX, int centerY) {
        ModConfig config = ConfigHandler.get();
        int slotCount = menu.getSlotCount();
        double slotAngle = 360.0 / slotCount;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (int i = 0; i < slotCount; i++) {
            MenuSlot slot = menu.getSlots().get(i);
            boolean isSelected = (i == menu.getSelectedIndex());

            double startAngle = i * slotAngle - slotAngle / 2;
            double endAngle = startAngle + slotAngle;

            int color = isSelected ? config.highlightColor : config.backgroundColor;
            drawSlotArc(context, centerX, centerY, config.innerRadius, config.outerRadius,
                    startAngle, endAngle, color);

            if (slot.hasAction()) {
                drawSlotLabel(context, slot, centerX, centerY,
                        (config.innerRadius + config.outerRadius) / 2,
                        startAngle + slotAngle / 2);
            }
        }

        drawBorders(context, centerX, centerY, config.innerRadius, config.outerRadius,
                slotCount, config.borderColor);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawSlotArc(DrawContext context, int centerX, int centerY,
            int innerRadius, int outerRadius, double startAngle, double endAngle, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        //? if >=1.21 {
        BufferBuilder buffer = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        //?} else {
        /*Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        *///?}

        for (int i = 0; i <= SEGMENTS_PER_SLOT; i++) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / SEGMENTS_PER_SLOT - 90);
            float xInner = centerX + (float)(Math.cos(angle) * innerRadius);
            float yInner = centerY + (float)(Math.sin(angle) * innerRadius);
            float xOuter = centerX + (float)(Math.cos(angle) * outerRadius);
            float yOuter = centerY + (float)(Math.sin(angle) * outerRadius);

            buffer.vertex(matrix, xInner, yInner, 0).color(r, g, b, a)
            //? if <1.21 {
            /*.next()
            *///?}
            ;
            buffer.vertex(matrix, xOuter, yOuter, 0).color(r, g, b, a)
            //? if <1.21 {
            /*.next()
            *///?}
            ;
        }

        //? if >=1.21 {
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        //?} else {
        /*tessellator.draw();
        *///?}
    }

    private void drawSlotLabel(DrawContext context, MenuSlot slot, int centerX, int centerY,
            int radius, double angle) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String label = slot.getShortLabel();

        double radians = Math.toRadians(angle - 90);
        int labelX = centerX + (int)(Math.cos(radians) * radius);
        int labelY = centerY + (int)(Math.sin(radians) * radius);

        int textWidth = textRenderer.getWidth(label);
        context.drawText(textRenderer, label, labelX - textWidth / 2, labelY - 4, 0xFFFFFFFF, true);
    }

    private void drawBorders(DrawContext context, int centerX, int centerY,
            int innerRadius, int outerRadius, int slotCount, int color) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        double slotAngle = 360.0 / slotCount;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        //? if >=1.21 {
        BufferBuilder buffer = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        //?} else {
        /*Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        *///?}

        for (int i = 0; i < slotCount; i++) {
            double angle = Math.toRadians(i * slotAngle - slotAngle / 2 - 90);
            float xInner = centerX + (float)(Math.cos(angle) * innerRadius);
            float yInner = centerY + (float)(Math.sin(angle) * innerRadius);
            float xOuter = centerX + (float)(Math.cos(angle) * outerRadius);
            float yOuter = centerY + (float)(Math.sin(angle) * outerRadius);

            buffer.vertex(matrix, xInner, yInner, 0).color(r, g, b, a)
            //? if <1.21 {
            /*.next()
            *///?}
            ;
            buffer.vertex(matrix, xOuter, yOuter, 0).color(r, g, b, a)
            //? if <1.21 {
            /*.next()
            *///?}
            ;
        }

        //? if >=1.21 {
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        //?} else {
        /*tessellator.draw();
        *///?}
    }
    //?} else {
    /*public void render(MatrixStack matrices, RadialMenu menu, int centerX, int centerY) {
        ModConfig config = ConfigHandler.get();
        int slotCount = menu.getSlotCount();
        double slotAngle = 360.0 / slotCount;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int i = 0; i < slotCount; i++) {
            MenuSlot slot = menu.getSlots().get(i);
            boolean isSelected = (i == menu.getSelectedIndex());

            double startAngle = i * slotAngle - slotAngle / 2;
            double endAngle = startAngle + slotAngle;

            int color = isSelected ? config.highlightColor : config.backgroundColor;
            drawSlotArc(matrices, buffer, tessellator, centerX, centerY,
                    config.innerRadius, config.outerRadius, startAngle, endAngle, color);

            if (slot.hasAction()) {
                drawSlotLabel(matrices, slot, centerX, centerY,
                        (config.innerRadius + config.outerRadius) / 2,
                        startAngle + slotAngle / 2);
            }
        }

        drawBorders(matrices, buffer, tessellator, centerX, centerY,
                config.innerRadius, config.outerRadius, slotCount, config.borderColor);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawSlotArc(MatrixStack matrices, BufferBuilder buffer, Tessellator tessellator,
            int centerX, int centerY, int innerRadius, int outerRadius,
            double startAngle, double endAngle, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int i = 0; i <= SEGMENTS_PER_SLOT; i++) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / SEGMENTS_PER_SLOT - 90);
            float xInner = centerX + (float)(Math.cos(angle) * innerRadius);
            float yInner = centerY + (float)(Math.sin(angle) * innerRadius);
            float xOuter = centerX + (float)(Math.cos(angle) * outerRadius);
            float yOuter = centerY + (float)(Math.sin(angle) * outerRadius);

            buffer.vertex(matrices.peek().getPositionMatrix(), xInner, yInner, 0).color(r, g, b, a).next();
            buffer.vertex(matrices.peek().getPositionMatrix(), xOuter, yOuter, 0).color(r, g, b, a).next();
        }

        tessellator.draw();
    }

    private void drawSlotLabel(MatrixStack matrices, MenuSlot slot, int centerX, int centerY,
            int radius, double angle) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        String label = slot.getShortLabel();

        double radians = Math.toRadians(angle - 90);
        int labelX = centerX + (int)(Math.cos(radians) * radius);
        int labelY = centerY + (int)(Math.sin(radians) * radius);

        int textWidth = textRenderer.getWidth(label);
        textRenderer.drawWithShadow(matrices, label, labelX - textWidth / 2f, labelY - 4, 0xFFFFFFFF);
    }

    private void drawBorders(MatrixStack matrices, BufferBuilder buffer, Tessellator tessellator,
            int centerX, int centerY, int innerRadius, int outerRadius, int slotCount, int color) {
        double slotAngle = 360.0 / slotCount;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (int i = 0; i < slotCount; i++) {
            double angle = Math.toRadians(i * slotAngle - slotAngle / 2 - 90);
            float xInner = centerX + (float)(Math.cos(angle) * innerRadius);
            float yInner = centerY + (float)(Math.sin(angle) * innerRadius);
            float xOuter = centerX + (float)(Math.cos(angle) * outerRadius);
            float yOuter = centerY + (float)(Math.sin(angle) * outerRadius);

            buffer.vertex(matrices.peek().getPositionMatrix(), xInner, yInner, 0).color(r, g, b, a).next();
            buffer.vertex(matrices.peek().getPositionMatrix(), xOuter, yOuter, 0).color(r, g, b, a).next();
        }

        tessellator.draw();
    }
    *///?}
}

