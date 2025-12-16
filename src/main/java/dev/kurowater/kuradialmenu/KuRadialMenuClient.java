package dev.kurowater.kuradialmenu;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KuRadialMenuClient implements ClientModInitializer {
    // このロガーはコンソールとログファイルにテキストを書き込むために使用されます。
    // Mod ID をロガー名として使用することがベストプラクティスとされています。
    // そうすることで、どの Mod が情報、警告、エラーを書き込んだかが明確になります。
    public static final Logger LOGGER = LoggerFactory.getLogger("kuradialmenu");
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.20.1";

    @Override
    public void onInitializeClient() {
        // このコードは Minecraft が Mod 読み込み可能な状態になるとすぐに実行されます。
        // ただし、一部の要素（リソースなど）はまだ初期化されていない可能性があります。
        // 慎重に進めてください。

        LOGGER.info("Hello Fabric world!");

        //? if !release
        /*LOGGER.warn("I'm still a template!");*/

        //? if fapi: <0.100
        LOGGER.info("Fabric API is old on this version");
    }

    /**
     * 1.21 で導入された {@link ResourceLocation} の変更に適応します。
     */
    public static ResourceLocation id(String namespace, String path) {
        //? if <1.21 {
        return new ResourceLocation(namespace, path);
        //?} else
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);*/
    }
}

