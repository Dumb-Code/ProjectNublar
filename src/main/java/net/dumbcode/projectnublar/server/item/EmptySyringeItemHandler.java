package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class EmptySyringeItemHandler {

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Item item = event.getItemStack().getItem();
        if(item == ItemHandler.EMPTY_SYRINGE.get() && event.getTarget() instanceof LivingEntity) {
            event.setCancellationResult(runInteraction(event.getItemStack(), event.getPlayer(), (LivingEntity) event.getTarget(), event.getHand()));
            if(event.getCancellationResult().consumesAction()) {
                event.setCanceled(true);
            }
        }
    }

    public static ActionResultType runInteraction(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        ItemStack out;
        if((entity instanceof ChickenEntity || entity instanceof ParrotEntity) && ((AnimalEntity) entity).isInLove()) {
            out = new ItemStack(ItemHandler.EMBRYO_FILLED_SYRINGE.get());
            out.getOrCreateTagElement(ProjectNublar.MODID).putString("ContainedType", entity.getType().getRegistryName().toString());
        } else if(EntityGeneticRegistry.INSTANCE.isRegistered(entity.getType())) {
            out = new ItemStack(ItemHandler.DNA_FILLED_SYRINGE.get());
            CompoundTag nbt = out.getOrCreateTagElement(ProjectNublar.MODID);
            nbt.putString("ContainedType", entity.getType().getRegistryName().toString());
            nbt.putInt("ContainedSize", MathUtils.getWeightedResult(65, 10));
            String variant = EntityGeneticRegistry.INSTANCE.getVariant(entity);
            if(variant != null) {
                nbt.putString("ContainedVariant", variant);
            }
        } else {
            return ActionResultType.PASS;
        }
        stack.shrink(1);
        if(stack.isEmpty()) {
            player.setItemInHand(Hand.MAIN_HAND, out);
        } else {
            MachineUtils.giveToInventory(player, out);
        }
        return ActionResultType.SUCCESS;
    }
}
