package dev.kurowater.kuradialmenu.mixin.client;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class KuRadialMenuMixin {
	@Inject(method = "loadLevel", at = @At("HEAD"))
	private void init(CallbackInfo info) {
		// このコードは MinecraftServer.loadWorld()V の開始時に注入されます
	}
}

