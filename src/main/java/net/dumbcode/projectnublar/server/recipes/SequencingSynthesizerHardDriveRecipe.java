package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.DriveItem;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum SequencingSynthesizerHardDriveRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity> {

    INSTANCE;

    private static final int HDD_TOTAL_CONSUME_TIME = 200;
    private static final int SSD_TOTAL_CONSUME_TIME = HDD_TOTAL_CONSUME_TIME / 2;

    @Override
    public boolean accepts(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        MachineModuleItemStackHandler<SequencingSynthesizerBlockEntity> handler = blockEntity.getHandler();
        ItemStack drive = handler.getStackInSlot(0);
        ItemStack in = handler.getStackInSlot(process.getInputSlot(0));

        return !drive.isEmpty() && drive.getItem() instanceof DriveItem &&
                in.getItem() instanceof DriveUtils.DriveInformation &&
                DriveUtils.canAdd(drive, in);
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        return ((DriveItem) blockEntity.getHandler().getStackInSlot(0).getItem()).isSsd() ? SSD_TOTAL_CONSUME_TIME : HDD_TOTAL_CONSUME_TIME;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        MachineModuleItemStackHandler<SequencingSynthesizerBlockEntity> handler = blockEntity.getHandler();
        ItemStack inStack = handler.getStackInSlot(process.getInputSlot(0));
        ItemStack out = inStack.split(1);
        if(!out.isEmpty()) {
            DriveUtils.addItemToDrive(handler.getStackInSlot(0), out);
        }
        if(out.getItem() instanceof DriveUtils.DriveInformation) {
            ItemStack outItem = ((DriveUtils.DriveInformation) out.getItem()).getOutItem(out);
            process.insertOutputItem(outItem, 0);
        }
    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        if (slotIndex == 0) {
            return testStack.getItem() instanceof DriveUtils.DriveInformation && ((DriveUtils.DriveInformation) testStack.getItem()).hasInformation(testStack);
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "sequencer_hard_drive");
    }

    @Override
    public int getCurrentConsumptionPerTick(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        return 35;
    }

    @Override
    public int getCurrentProductionPerTick(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        return 0;
    }
}
