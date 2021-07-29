package net.dumbcode.projectnublar.server.recipes;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Map;

public enum SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity>{
    INSTANCE;

    private final ResourceLocation registryName = new ResourceLocation(ProjectNublar.MODID, "dna_creation");

    @Override
    public boolean accepts(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        MachineModuleItemStackHandler<SequencingSynthesizerBlockEntity> handler = blockEntity.getHandler();
        ItemStack testTube = handler.getStackInSlot(process.getInputSlot(0));

        double storageSize = SequencingSynthesizerBlockEntity.DEFAULT_STORAGE;

        if(blockEntity.getTank().getFluidAmount() >= FluidAttributes.BUCKET_VOLUME / 2 &&
                blockEntity.getPlantAmount() >= storageSize / 2 &&
                blockEntity.getBoneAmount() >= storageSize / 2 &&
                blockEntity.getSugarAmount() >= storageSize / 2 &&
                testTube.getItem() == ItemHandler.EMPTY_TEST_TUBE.get()) {
            CompoundNBT nbt = handler.getStackInSlot(0).getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information");

            Map<String, Double> amountMap = Maps.newHashMap();

            amountMap.put(blockEntity.getSelectKey(1), blockEntity.getSelectAmount(1));
            amountMap.put(blockEntity.getSelectKey(2), blockEntity.getSelectAmount(2));
            amountMap.put(blockEntity.getSelectKey(3), blockEntity.getSelectAmount(3));

            for (Map.Entry<String, Double> entry : amountMap.entrySet()) {
                double amountIn = nbt.getCompound(entry.getKey()).getInt("amount") / 100D;
                if(amountIn < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean startsAutomatically() {
        return false;
    }

    @Override
    public boolean shouldGlobalSlotChangeCauseReset(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process, int slot) {
        return slot == 0; //Hard Drive Changed.
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        return 12000 - 3600*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        MachineModuleItemStackHandler<SequencingSynthesizerBlockEntity> handler = blockEntity.getHandler();
        handler.getStackInSlot(process.getInputSlot(0)).shrink(1);

        process.insertOutputItem(this.createStack(blockEntity), 0);


        double storageSize = SequencingSynthesizerBlockEntity.DEFAULT_STORAGE;

        blockEntity.getTank().drain(FluidAttributes.BUCKET_VOLUME / 2, IFluidHandler.FluidAction.EXECUTE);
        blockEntity.setBoneAmount(blockEntity.getBoneAmount() - storageSize / 2);
        blockEntity.setPlantAmount(blockEntity.getPlantAmount() - storageSize / 2);
        blockEntity.setSugarAmount(blockEntity.getSugarAmount() - storageSize / 2);

    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        switch (slotIndex) {
            case 0: return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE.get();
        }
        return false;
    }

    private ItemStack createStack(SequencingSynthesizerBlockEntity blockEntity) {
        if(blockEntity.getLevel().isClientSide) {
            return ItemStack.EMPTY;
        }
        CompoundNBT driveNbt = blockEntity.getHandler().getStackInSlot(0).getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information");

        String key = blockEntity.getSelectKey(1);
        if(!key.isEmpty() && DinosaurHandler.getRegistry().containsKey(new ResourceLocation(key))) {
            ItemStack out = new ItemStack(ItemHandler.TEST_TUBES_DNA.get(DinosaurHandler.getRegistry().getValue(new ResourceLocation(key))).get());
            CompoundNBT nbt = out.getOrCreateTagElement(ProjectNublar.MODID).getCompound("dna_info");
            for (int i = 1; i < 4; i++) {
                String dKey = blockEntity.getSelectKey(i);
                CompoundNBT tag = new CompoundNBT();
                tag.putString("translation_key", driveNbt.getCompound(dKey).getString("translation_key"));
                tag.putInt("amount", (int) (blockEntity.getSelectAmount(i) * 100D));
                nbt.put(dKey, tag);

                CompoundNBT driveTag = driveNbt.getCompound(dKey);
                int amount = driveTag.getInt("amount") - (int)(blockEntity.getSelectAmount(i) * 100D);
                if(amount <= 0) {
                    driveNbt.remove(dKey);
                } else {
                    driveTag.putInt("amount", amount);
                }

            }
            return out;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 0;
    }
}
