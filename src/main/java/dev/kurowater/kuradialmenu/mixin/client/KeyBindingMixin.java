package dev.kurowater.kuradialmenu.mixin.client;

import dev.kurowater.kuradialmenu.client.model.MenuAction;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * KeyBinding の timesPressed フィールドにアクセスするための Mixin
 */
@Mixin(KeyBinding.class)
public class KeyBindingMixin implements MenuAction.KeyBindingAccessor {

    @Shadow
    private int timesPressed;

    @Override
    public void kuradialmenu$incrementTimesPressed() {
        this.timesPressed++;
    }
}

