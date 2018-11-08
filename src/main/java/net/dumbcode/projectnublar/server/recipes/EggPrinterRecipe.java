package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum  EggPrinterRecipe implements MachineRecipe<EggPrinterBlockEntity> {
    INSTANCE
    ;

    @Override
    public boolean accepts(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack dna = handler.getStackInSlot(process.getInputSlots()[0]);
        Item dnaItem = dna.getItem();
        ItemStack boneStack = handler.getStackInSlot(process.getInputSlots()[2]);
        if(dnaItem instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_DNA.containsValue(dnaItem) && handler.getStackInSlot(process.getInputSlots()[1]).getItem() == ItemHandler.EMBRYO_FILLED_SYRINGE && boneStack.getItem() == Items.DYE && boneStack.getMetadata() == 15 && boneStack.getCount() >= 5) {
            return handler.insertOutputItem(process.getOutputSlots()[1], new ItemStack(ItemHandler.EMPTY_SYRINGE), true).isEmpty() && handler.insertOutputItem(process.getOutputSlots()[0], this.createStack(dna), true).isEmpty();
        }
        return false;
    }

    private ItemStack createStack(ItemStack dna) {
        ItemStack out = ItemStack.EMPTY;
        if(dna.getItem() instanceof DinosaurProvider) {
            out = new ItemStack(ItemHandler.DINOSAUR_UNINCUBATED_EGG.get(((DinosaurProvider) dna.getItem()).getDinosaur()));
            out.getOrCreateSubCompound(ProjectNublar.MODID).setTag("dna_info", dna.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("dna_info").copy());
        }
        return out;
    }

    @Override
    public int getRecipeTime(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 50;
    }

    @Override
    public void onRecipeFinished(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();

        handler.insertOutputItem(process.getOutputSlots()[1], new ItemStack(ItemHandler.EMPTY_SYRINGE), false);
        handler.insertOutputItem(process.getOutputSlots()[0], this.createStack(handler.getStackInSlot(process.getInputSlots()[0])), false);


        handler.getStackInSlot(process.getInputSlots()[0]).shrink(1);
        handler.getStackInSlot(process.getInputSlots()[1]).shrink(1);
        handler.getStackInSlot(process.getInputSlots()[2]).shrink(5);


    }

    @Override
    public boolean acceptsInputSlot(EggPrinterBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        Item item = testStack.getItem();
        switch (slotIndex) {
            case 0: return item instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_DNA.containsValue(item);
            case 1: return item == ItemHandler.EMBRYO_FILLED_SYRINGE;
            case 2: return item == Items.DYE && testStack.getMetadata() == 15;
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "egg_printing");
    }
}
