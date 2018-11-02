package net.dumbcode.projectnublar.server.recipes;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Map;
import java.util.function.Predicate;

public enum SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity>{
    INSTANCE;

    private final ResourceLocation registryName = new ResourceLocation(ProjectNublar.MODID, "dna_creation");

    @Override
    public boolean accpets(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack testTube = handler.getStackInSlot(process.getInputSlots()[0]);
        ItemStack sugar = handler.getStackInSlot(process.getInputSlots()[1]);


        if(blockEntity.getTank().getFluidAmount() >= Fluid.BUCKET_VOLUME / 3 && testTube.getItem() == ItemHandler.EMPTY_TEST_TUBE && sugar.getItem() == Items.SUGAR && sugar.getCount() >= 32) {
            Map<String, Double> amountMap = Maps.newHashMap();

            amountMap.put(blockEntity.getSelectKey(1), blockEntity.getSelectAmount(1));
            amountMap.put(blockEntity.getSelectKey(2), blockEntity.getSelectAmount(2));
            amountMap.put(blockEntity.getSelectKey(3), blockEntity.getSelectAmount(3));

        }

        return false;
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 40;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {

    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        return true;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}
