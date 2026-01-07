package dev.kurowater.kuradialmenu;

import dev.kurowater.kuradialmenu.client.config.ConfigHandler;
import dev.kurowater.kuradialmenu.client.keybind.KeybindHandler;
import dev.kurowater.kuradialmenu.client.keybind.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KuRadialMenuClient implements ClientModInitializer {

    public static final String MOD_ID = "kuradialmenu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {} v{} for Minecraft {}", MOD_ID, VERSION, MINECRAFT);

        // 設定システムの初期化
        ConfigHandler.initialize();

        // キーバインドの登録
        ModKeybinds.register();

        // キーバインドハンドラーの登録
        KeybindHandler.register();

        LOGGER.info("{} initialized successfully!", MOD_ID);
    }

    /**
     * 1.21 で導入された {@link Identifier} の変更に適応します。
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new Identifier(namespace, path);
        *///?} else
        return Identifier.of(namespace, path);
    }
}

