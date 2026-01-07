package dev.kurowater.kuradialmenu.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

/**
 * YACL を使用した設定画面を構築するクラス
 */
public final class ModConfigScreen {

    private ModConfigScreen() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * 設定画面を作成
     *
     * @param parent 親画面
     * @return 設定画面
     */
    public static Screen create(Screen parent) {
        ModConfig config = ConfigHandler.get();
        ModConfig defaults = ConfigHandler.getDefaults();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.kuradialmenu.title"))
                .category(createGeneralCategory(config, defaults))
                .category(createUICategory(config, defaults))
                .save(ConfigHandler::save)
                .build()
                .generateScreen(parent);
    }

    /**
     * 一般設定カテゴリを作成
     */
    private static ConfigCategory createGeneralCategory(ModConfig config, ModConfig defaults) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.kuradialmenu.category.general"))
                .tooltip(Text.translatable("config.kuradialmenu.category.general.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.kuradialmenu.group.menu"))
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.slotCount"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.slotCount.description")))
                                .binding(
                                        defaults.slotCount,
                                        () -> config.slotCount,
                                        value -> {
                                            config.slotCount = value;
                                            config.adjustSlotActionsSize();
                                        })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(4, 12)
                                        .step(4))
                                .build())
                        .build())
                .build();
    }

    /**
     * UI設定カテゴリを作成
     */
    private static ConfigCategory createUICategory(ModConfig config, ModConfig defaults) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.kuradialmenu.category.ui"))
                .tooltip(Text.translatable("config.kuradialmenu.category.ui.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.kuradialmenu.group.size"))
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.innerRadius"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.innerRadius.description")))
                                .binding(
                                        defaults.innerRadius,
                                        () -> config.innerRadius,
                                        value -> config.innerRadius = value)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(10, 100)
                                        .step(5))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.outerRadius"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.outerRadius.description")))
                                .binding(
                                        defaults.outerRadius,
                                        () -> config.outerRadius,
                                        value -> config.outerRadius = value)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(50, 300)
                                        .step(10))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.centerDeadzone"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.centerDeadzone.description")))
                                .binding(
                                        defaults.centerDeadzone,
                                        () -> config.centerDeadzone,
                                        value -> config.centerDeadzone = value)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(5, 50)
                                        .step(5))
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.kuradialmenu.group.colors"))
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.backgroundColor"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.backgroundColor.description")))
                                .binding(
                                        new Color(defaults.backgroundColor, true),
                                        () -> new Color(config.backgroundColor, true),
                                        value -> config.backgroundColor = value.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.highlightColor"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.highlightColor.description")))
                                .binding(
                                        new Color(defaults.highlightColor, true),
                                        () -> new Color(config.highlightColor, true),
                                        value -> config.highlightColor = value.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("config.kuradialmenu.option.borderColor"))
                                .description(OptionDescription.of(
                                        Text.translatable("config.kuradialmenu.option.borderColor.description")))
                                .binding(
                                        new Color(defaults.borderColor, true),
                                        () -> new Color(config.borderColor, true),
                                        value -> config.borderColor = value.getRGB())
                                .controller(opt -> ColorControllerBuilder.create(opt)
                                        .allowAlpha(true))
                                .build())
                        .build())
                .build();
    }
}

