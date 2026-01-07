package dev.kurowater.kuradialmenu.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kurowater.kuradialmenu.KuRadialMenuClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 設定の読み書きを管理するハンドラークラス
 * Gson を使用して JSON シリアライズを行う
 */
public final class ConfigHandler {

    private static final String CONFIG_FILE_NAME = "kuradialmenu.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ModConfig config;
    private static ModConfig defaults;
    private static Path configPath;

    private ConfigHandler() {
        // ユーティリティクラスのためインスタンス化禁止
    }

    /**
     * 設定ハンドラーを初期化し設定ファイルを読み込む
     */
    public static void initialize() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        defaults = new ModConfig();

        // 設定ファイルを読み込む
        load();

        // 設定の妥当性検証
        config.validate();

        // 検証後の設定を保存
        save();

        KuRadialMenuClient.LOGGER.info("Config loaded from {}", configPath);
    }

    /**
     * 現在の設定インスタンスを取得
     * @return 設定インスタンス
     */
    public static ModConfig get() {
        if (config == null) {
            throw new IllegalStateException("ConfigHandler has not been initialized");
        }
        return config;
    }

    /**
     * デフォルト設定インスタンスを取得
     * @return デフォルト設定インスタンス
     */
    public static ModConfig getDefaults() {
        if (defaults == null) {
            defaults = new ModConfig();
        }
        return defaults;
    }

    /**
     * 設定ファイルから読み込み
     */
    public static void load() {
        if (configPath == null) {
            config = new ModConfig();
            return;
        }

        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                config = GSON.fromJson(json, ModConfig.class);
                if (config == null) {
                    config = new ModConfig();
                }
            } catch (IOException e) {
                KuRadialMenuClient.LOGGER.error("Failed to load config", e);
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
        }
    }

    /**
     * 設定ファイルに保存
     */
    public static void save() {
        if (configPath == null || config == null) {
            return;
        }

        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(config);
            Files.writeString(configPath, json);
        } catch (IOException e) {
            KuRadialMenuClient.LOGGER.error("Failed to save config", e);
        }
    }
}

