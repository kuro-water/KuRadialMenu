package dev.kurowater.kuradialmenu.client.config;

import com.google.gson.JsonParseException;
import dev.kurowater.kuradialmenu.KuRadialMenuClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/** Owns the active config and prevents failed reads from destroying user data. */
public final class ConfigHandler {
    private static final String CONFIG_FILE_NAME = "kuradialmenu.json";

    private static ModConfig config;
    private static Path configPath;
    private static boolean writable;
    private static boolean loadedSuccessfully;
    private static String lastError;

    private ConfigHandler() {
    }

    public static void initialize() {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        load();
        // A damaged or unreadable original is never replaced during startup.
        if (loadedSuccessfully) {
            save();
        }
    }

    public static ModConfig get() {
        if (config == null) {
            throw new IllegalStateException("ConfigHandler has not been initialized");
        }
        return config;
    }

    /** Return a new instance so a caller cannot mutate future reset values. */
    public static ModConfig getDefaults() {
        return new ModConfig();
    }

    public static String getLastError() {
        return lastError;
    }

    public static void load() {
        config = new ModConfig();
        writable = false;
        loadedSuccessfully = false;
        lastError = null;
        if (configPath == null) {
            lastError = "ConfigHandler has not been initialized";
            return;
        }
        try {
            config = ConfigFileStore.read(configPath);
            writable = true;
            loadedSuccessfully = true;
            KuRadialMenuClient.LOGGER.info("Config loaded from {}", configPath);
        } catch (NoSuchFileException e) {
            writable = true;
            loadedSuccessfully = true;
        } catch (JsonParseException e) {
            lastError = "Invalid config; defaults are active. Original preserved at " + configPath;
            KuRadialMenuClient.LOGGER.error(lastError, e);
            try {
                Path backup = ConfigFileStore.backupInvalid(configPath);
                writable = true;
                lastError = "Invalid config backed up at " + backup + ". Save settings to recover.";
                KuRadialMenuClient.LOGGER.warn(lastError);
            } catch (IOException backupError) {
                lastError += ". Backup failed; saving is disabled until the file is repaired and reloaded.";
                KuRadialMenuClient.LOGGER.error(lastError, backupError);
            }
        } catch (IOException e) {
            lastError = "Cannot read " + configPath + "; saving is disabled until it is readable and reloaded.";
            KuRadialMenuClient.LOGGER.error(lastError, e);
        }
    }

    /** Compatibility callback for YACL; callers needing feedback use trySave(). */
    public static void save() {
        trySave();
    }

    public static boolean trySave() {
        if (configPath == null || config == null || !writable) {
            if (lastError == null) {
                lastError = "Config is not writable";
            }
            KuRadialMenuClient.LOGGER.error("Cannot save config: {}", lastError);
            return false;
        }
        try {
            ConfigFileStore.write(configPath, config);
            lastError = null;
            return true;
        } catch (IOException e) {
            lastError = "Failed to save " + configPath + ": " + e.getMessage();
            KuRadialMenuClient.LOGGER.error(lastError, e);
            return false;
        }
    }
}
