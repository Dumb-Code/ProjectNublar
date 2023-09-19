package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.server.utils.PickupUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Inject(method = "add(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void interceptItems(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (PickupUtil.interceptItem((PlayerInventory) (Object) this, stack)) cir.setReturnValue(true);
    }
}