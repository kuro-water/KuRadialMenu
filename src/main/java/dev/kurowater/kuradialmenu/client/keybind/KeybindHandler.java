package dev.kurowater.kuradialmenu.client.keybind;

import dev.kurowater.kuradialmenu.client.config.ModConfigScreen;
import dev.kurowater.kuradialmenu.client.model.MenuAction.KeyBindingAccessor;
import dev.kurowater.kuradialmenu.client.ui.RadialMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;

/** Owns radial input and cleans up state on cancellation, focus loss and disconnect. */
public final class KeybindHandler {
    private static final MenuKeyState MENU_KEY = new MenuKeyState();
    private static boolean movementOverridden;
    private static ClientWorld lastWorld;

    private KeybindHandler() { }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(KeybindHandler::beforeTick);
        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::afterTick);
    }

    private static boolean available(MinecraftClient client) {
        return client.player != null && client.world != null && client.isWindowFocused();
    }

    private static KeyBinding[] movementKeys(MinecraftClient client) {
        return new KeyBinding[] { client.options.forwardKey, client.options.backKey,
                client.options.leftKey, client.options.rightKey, client.options.jumpKey,
                client.options.sneakKey, client.options.sprintKey };
    }

    private static void beforeTick(MinecraftClient client) {
        boolean active = available(client) && lastWorld == client.world;
        lastWorld = client.world;
        ActionDispatcher.beforeTick(client, active);
        if (!active) {
            MENU_KEY.update(false, false);
            ActionDispatcher.finishTick(client, false);
            if (client.currentScreen instanceof RadialMenuScreen) {
                client.setScreen(null); // Cancellation never executes the highlighted action.
            }
        }
        if (active && client.currentScreen instanceof RadialMenuScreen) {
            for (KeyBinding binding : movementKeys(client)) {
                // Bypass StickyKeyBinding#setPressed: polling must not toggle sneak every tick.
                ((KeyBindingAccessor) binding).kuradialmenu$setPressed(PhysicalInput.isDown(client, binding));
            }
            movementOverridden = true;
        } else if (movementOverridden) {
            releaseMovement(client);
        }
    }

    private static void releaseMovement(MinecraftClient client) {
        if (!movementOverridden) {
            return;
        }
        boolean restore = available(client) && client.currentScreen == null;
        for (KeyBinding binding : movementKeys(client)) {
            ((KeyBindingAccessor) binding).kuradialmenu$setPressed(restore && PhysicalInput.isDown(client, binding));
        }
        movementOverridden = false;
    }

    /** Screen.removed runs before Minecraft assigns the replacement screen. */
    public static void menuRemoved(MinecraftClient client) {
        if (movementOverridden) {
            for (KeyBinding binding : movementKeys(client)) {
                ((KeyBindingAccessor) binding).kuradialmenu$setPressed(false);
            }
            // Restore physical movement on the next tick only if returning to gameplay.
        }
    }

    private static void afterTick(MinecraftClient client) {
        boolean active = available(client);
        ActionDispatcher.finishTick(client, active);
        boolean openConfig = false;
        while (ModKeybinds.OPEN_CONFIG.wasPressed()) {
            openConfig = true;
        }
        // The radial key is monitored physically; drain its otherwise unused queued presses.
        while (ModKeybinds.OPEN_MENU.wasPressed()) { }

        MenuKeyState.Change change = MENU_KEY.update(active && PhysicalInput.isDown(client, ModKeybinds.OPEN_MENU), active);
        if (!active) {
            return;
        }
        if (openConfig && client.currentScreen == null) {
            client.setScreen(ModConfigScreen.create(null));
        }
        if (change == MenuKeyState.Change.PRESSED && client.currentScreen == null) {
            client.setScreen(new RadialMenuScreen());
        } else if (change == MenuKeyState.Change.RELEASED
                && client.currentScreen instanceof RadialMenuScreen menuScreen) {
            menuScreen.updateSelectionFromMouse();
            client.setScreen(null);
            releaseMovement(client);
            menuScreen.executeSelectedAction();
        }
    }
}
