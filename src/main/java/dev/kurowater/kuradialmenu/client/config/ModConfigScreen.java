package dev.kurowater.kuradialmenu.client.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.CyclingListControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.MinecraftClient;
import dev.kurowater.kuradialmenu.client.ui.KeybindPickerScreen;
import dev.kurowater.kuradialmenu.client.ui.ConfigSaveErrorScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

    private static boolean errorHandlerRegistered;
    private static Screen pendingSaveError;

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

        if (!errorHandlerRegistered) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (pendingSaveError != null) {
                    Screen error = pendingSaveError;
                    pendingSaveError = null;
                    client.setScreen(error);
                }
            });
            errorHandlerRegistered = true;
        }
        Option<Integer> slotCount = Option.<Integer>createBuilder()
                .name(Text.translatable("config.kuradialmenu.option.slotCount"))
                .description(OptionDescription.of(Text.translatable("config.kuradialmenu.option.slotCount.description")))
                .binding(defaults.slotCount, () -> config.slotCount, value -> config.slotCount = value)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 12).step(4))
                .build();

        List<ConfigCategory> categories = List.of(createGeneralCategory(slotCount),
                createUICategory(config, defaults), createSlotCategory(config, defaults, slotCount));
        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("config.kuradialmenu.title"))
                .category(categories.get(0))
                .category(categories.get(1))
                .category(categories.get(2))
                .save(() -> {
                    if (!ConfigHandler.trySave()) {
                        pendingSaveError = new ConfigSaveErrorScreen(parent, ConfigHandler.getLastError());
                    }
                    // Validation can clamp radii or truncate slots. Synchronize pending values
                    // so YACL does not keep showing discarded values as unsaved changes.
                    for (ConfigCategory category : categories) {
                        for (OptionGroup group : category.groups()) {
                            group.options().forEach(Option::forgetPendingValue);
                        }
                    }
                })
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
                if (!keyBinding.getTranslationKey().equals("key.kuradialmenu.open_menu")) {
                    keyBindings.add(keyBinding.getTranslationKey());
                }
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
        return KeybindPickerScreen.displayName(translationKey);
    }

    /**
     * 一般設定カテゴリを作成
     */
    private static ConfigCategory createGeneralCategory(Option<Integer> slotCount) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("config.kuradialmenu.category.general"))
                .tooltip(Text.translatable("config.kuradialmenu.category.general.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("config.kuradialmenu.group.menu"))
                        .option(slotCount)
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
    private static ConfigCategory createSlotCategory(ModConfig config, ModConfig defaults, Option<Integer> slotCount) {
        List<String> availableKeyBindings = getAvailableKeyBindings();

        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .name(Text.translatable("config.kuradialmenu.category.slots"))
                .tooltip(Text.translatable("config.kuradialmenu.category.slots.tooltip"));

        // Keep all supported slots present; enable them from the pending (not yet saved) count.
        OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                .name(Text.translatable("config.kuradialmenu.group.slotActions"));

        for (int i = 0; i < 12; i++) {
            final int slotIndex = i;
            String defaultAction = slotIndex < defaults.slotActions.size() ? defaults.slotActions.get(slotIndex) : "";

            Option<String> assignment = Option.<String>createBuilder()
                    .name(Text.translatable("config.kuradialmenu.slot.number", slotIndex + 1))
                    .available(slotIndex < slotCount.pendingValue())
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
                    .build();
            ButtonOption search = ButtonOption.createBuilder()
                    .name(Text.translatable("config.kuradialmenu.slot.search", slotIndex + 1))
                    .text(Text.translatable("config.kuradialmenu.picker.open"))
                    .available(slotIndex < slotCount.pendingValue())
                    .action((screen, button) -> MinecraftClient.getInstance().setScreen(
                            new KeybindPickerScreen(screen, assignment.pendingValue(), assignment::requestSet)))
                    .build();
            slotCount.addListener((option, count) -> {
                assignment.setAvailable(slotIndex < count);
                search.setAvailable(slotIndex < count);
            });
            groupBuilder.option(assignment).option(search);
        }

        categoryBuilder.group(groupBuilder.build());
        return categoryBuilder.build();
    }

}
