package dev.kurowater.kuradialmenu.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kurowater.kuradialmenu.client.config.ModConfigScreen;

/**
 * Mod Menu との連携クラス。
 * Mod Menu から設定画面を開くための機能を提供する。
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigScreen::create;
    }
}

