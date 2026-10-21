package dev.kurowater.kuradialmenu.mixin.client;

import dev.kurowater.kuradialmenu.client.model.MenuAction;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps synthetic presses separate from the user's real queued key events. */
@Mixin(KeyBinding.class)
public class KeyBindingMixin implements MenuAction.KeyBindingAccessor {
    @Shadow
    private boolean pressed;

    @Unique
    private boolean kuradialmenu$queuedPress;

    @Override
    public void kuradialmenu$queuePress() {
        kuradialmenu$queuedPress = true;
    }

    @Inject(method = "wasPressed", at = @At("RETURN"), cancellable = true)
    private void kuradialmenu$consumePress(CallbackInfoReturnable<Boolean> callback) {
        // Let vanilla consume physical events first. Never clear or decrement its queue.
        if (!callback.getReturnValue() && kuradialmenu$queuedPress) {
            kuradialmenu$queuedPress = false;
            callback.setReturnValue(true);
        }
    }

    @Override
    public void kuradialmenu$setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public void kuradialmenu$clearQueuedPress() {
        kuradialmenu$queuedPress = false;
    }
}
