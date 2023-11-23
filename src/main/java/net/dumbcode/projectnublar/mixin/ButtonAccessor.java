package net.dumbcode.projectnublar.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Button.class)
public class ButtonAccessor {
    @Invoker("<init>")
    public static Button construct(int x, int y, int width, int height, Component text, Button.OnPress onPress, Button.CreateNarration narrationSupplier) {
        throw new AssertionError();
    }
}
