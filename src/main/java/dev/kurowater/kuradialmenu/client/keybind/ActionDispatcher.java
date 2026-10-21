package dev.kurowater.kuradialmenu.client.keybind;

import dev.kurowater.kuradialmenu.client.config.ModConfigScreen;
import dev.kurowater.kuradialmenu.client.model.MenuAction.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.ScreenshotRecorder;

/** A single game-tick pulse, rather than an indefinite simulated held key. */
public final class ActionDispatcher {
    private static KeyBinding pending;
    private static KeyBinding queued;

    private ActionDispatcher() { }

    public static void execute(MinecraftClient client, KeyBinding binding) {
        if (client.player == null || client.world == null || !client.isWindowFocused()) {
            return;
        }
        if (binding == ModKeybinds.OPEN_MENU) {
            return; // A menu must not recursively trigger itself.
        }
        if (binding == ModKeybinds.OPEN_CONFIG) {
            client.setScreen(ModConfigScreen.create(null));
            return;
        }
        // These are handled by Keyboard directly; incrementing timesPressed cannot trigger them.
        if (binding == client.options.screenshotKey) {
            ScreenshotRecorder.saveScreenshot(client.runDirectory, client.getFramebuffer(),
                    message -> client.execute(() -> client.inGameHud.getChatHud().addMessage(message)));
            return;
        }
        if (binding == client.options.fullscreenKey) {
            client.getWindow().toggleFullscreen();
            client.options.getFullscreen().setValue(client.getWindow().isFullscreen());
            return;
        }
        finishTick(client, true);
        if (queued != null) {
            ((KeyBindingAccessor) queued).kuradialmenu$clearQueuedPress();
        }
        pending = binding;
        queued = binding;
        KeyBindingAccessor accessor = (KeyBindingAccessor) binding;
        accessor.kuradialmenu$queuePress();
        accessor.kuradialmenu$setPressed(true);
    }

    /** Called at END_CLIENT_TICK before dispatching this tick's selected action. */
    public static void finishTick(MinecraftClient client, boolean available) {
        if (pending != null) {
            KeyBindingAccessor accessor = (KeyBindingAccessor) pending;
            accessor.kuradialmenu$setPressed(available && client.currentScreen == null
                    && PhysicalInput.isDown(client, pending));
            pending = null;
        }
    }

    /** Runs after all END_CLIENT_TICK listeners had a chance to consume the previous pulse. */
    public static void beforeTick(MinecraftClient client, boolean available) {
        if (!available || client.currentScreen != null) {
            finishTick(client, false);
        }
        if (queued != null && (pending == null || !available || client.currentScreen != null)) {
            ((KeyBindingAccessor) queued).kuradialmenu$clearQueuedPress();
            queued = null;
        }
    }
}
