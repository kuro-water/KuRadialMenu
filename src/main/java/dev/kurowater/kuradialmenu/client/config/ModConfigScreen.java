package dev.kurowater.kuradialmenu.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
                .category(createSlotCategory(config, defaults))
                .save(ConfigHandler::save)
                .build()
                .generateScreen(parent);
    }

    /**
     * 利用可能なキーバインド ID のリストを取得
     */
    private static List<String> getAvailableKeyBindings() {
        List<String> keyBindings = new ArrayList<>();
        keyBindings.add(""); // 空（未設定）

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options != null) {
            for (KeyBinding keyBinding : client.options.allKeys) {
                keyBindings.add(keyBinding.getTranslationKey());
            }
        }

        // 翻訳キーでソート
        keyBindings.sort(Comparator.naturalOrder());
        return keyBindings;
    }

    /**
     * キーバインド ID から表示名を取得
     */
    private static Text getKeyBindingDisplayName(String translationKey) {
        if (translationKey == null || translationKey.isEmpty()) {
            return Text.translatable("config.kuradialmenu.slot.none");
        }
        return Text.translatable(translationKey);
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

    /**
     * スロット設定カテゴリを作成
     */
    private static ConfigCategory createSlotCategory(ModConfig config, ModConfig defaults) {
        List<String> availableKeyBindings = getAvailableKeyBindings();

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Text.translatable("config.kuradialmenu.category.slots"))
                .tooltip(Text.translatable("config.kuradialmenu.category.slots.tooltip"));

        // 現在のスロット数に基づいてオプションを生成
        OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                .name(Text.translatable("config.kuradialmenu.group.slotActions"));

        for (int i = 0; i < config.slotCount; i++) {
            final int slotIndex = i;
            String defaultAction = slotIndex < defaults.slotActions.size() ? defaults.slotActions.get(slotIndex) : "";

            groupBuilder.option(Option.<String>createBuilder()
                    .name(Text.translatable("config.kuradialmenu.slot.index", slotIndex + 1, getSlotDirectionName(slotIndex, config.slotCount)))
                    .description(OptionDescription.of(
                            Text.translatable("config.kuradialmenu.slot.description")))
                    .binding(
                            defaultAction,
                            () -> slotIndex < config.slotActions.size() ? config.slotActions.get(slotIndex) : "",
                            value -> {
                                while (config.slotActions.size() <= slotIndex) {
                                    config.slotActions.add("");
                                }
                                config.slotActions.set(slotIndex, value);
                            })
                    .controller(opt -> CyclingListControllerBuilder.create(opt)
                            .values(availableKeyBindings)
                            .valueFormatter(ModConfigScreen::getKeyBindingDisplayName))
                    .build());
        }

        categoryBuilder.group(groupBuilder.build());
        return categoryBuilder.build();
    }

    /**
     * スロットインデックスから方向名を取得
     */
    private static String getSlotDirectionName(int index, int slotCount) {
        String[] directions8 = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
        String[] directions4 = {"↑", "→", "↓", "←"};
        String[] directions12 = {"↑", "↗", "↗", "→", "↘", "↘", "↓", "↙", "↙", "←", "↖", "↖"};

        return switch (slotCount) {
            case 4 -> index < directions4.length ? directions4[index] : "";
            case 12 -> index < directions12.length ? directions12[index] : "";
            default -> index < directions8.length ? directions8[index] : "";
        };
    }
}
