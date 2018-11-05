package net.dumbcode.projectnublar.server.recipes;

import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Map;
import java.util.function.Predicate;

public enum SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity>{
    INSTANCE;

    private final ResourceLocation registryName = new ResourceLocation(ProjectNublar.MODID, "dna_creation");

    @Override
    public boolean accepts(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack testTube = handler.getStackInSlot(process.getInputSlots()[0]);

        if(blockEntity.getTank().getFluidAmount() >= Fluid.BUCKET_VOLUME / 6 && blockEntity.getPlantAmount() >= 1 && blockEntity.getBoneAmount() >= 1 && blockEntity.getSugarAmount() >= 1 && testTube.getItem() == ItemHandler.EMPTY_TEST_TUBE) {
            NBTTagCompound nbt = handler.getStackInSlot(0).getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");

            Map<String, Double> amountMap = Maps.newHashMap();

            amountMap.put(blockEntity.getSelectKey(1), blockEntity.getSelectAmount(1));
            amountMap.put(blockEntity.getSelectKey(2), blockEntity.getSelectAmount(2));
            amountMap.put(blockEntity.getSelectKey(3), blockEntity.getSelectAmount(3));

            for (Map.Entry<String, Double> entry : amountMap.entrySet()) {
                double amountIn = nbt.getCompoundTag(entry.getKey()).getInteger("amount") / 100D;
                if(amountIn < entry.getValue()) {
                    return false;
                }
            }
            return handler.insertOutputItem(process.getOutputSlots()[0], this.createStack(blockEntity, true), true).isEmpty();

        }

        return false;
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 40;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        handler.getStackInSlot(process.getInputSlots()[0]).shrink(1);

        blockEntity.getTank().drainInternal(Fluid.BUCKET_VOLUME / 6, true);

        handler.insertOutputItem(process.getOutputSlots()[0], this.createStack(blockEntity, false), false);

        blockEntity.setBoneAmount(0);
        blockEntity.setPlantAmount(0);
        blockEntity.setSugarAmount(0);

    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        switch (slotIndex) {
            case 0: return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
        }
        return false;
    }

    private ItemStack createStack(SequencingSynthesizerBlockEntity blockEntity, boolean testOnly) {
        if(blockEntity.getWorld().isRemote) {
            return ItemStack.EMPTY;
        }
        NBTTagCompound driveNbt = blockEntity.getHandler().getStackInSlot(0).getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");

        String key = blockEntity.getSelectKey(1);
        if(!key.isEmpty() && ProjectNublar.DINOSAUR_REGISTRY.containsKey(new ResourceLocation(key))) {
            ItemStack out = new ItemStack(ItemHandler.TEST_TUBES_DNA.get(ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(key))));
            NBTTagCompound nbt = out.getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("dna_info");
            for (int i = 1; i < 4; i++) {
                String dKey = blockEntity.getSelectKey(i);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("translation_key", driveNbt.getCompoundTag(dKey).getString("translation_key"));
                tag.setInteger("amount", (int) (blockEntity.getSelectAmount(i) * 100D));
                nbt.setTag(dKey, tag);
                if(!testOnly) {
                    NBTTagCompound driveTag = driveNbt.getCompoundTag(dKey);
                    int amount = driveTag.getInteger("amount") - (int)(blockEntity.getSelectAmount(i) * 100D);
                    if(amount <= 0) {
                        driveNbt.removeTag(dKey);
                    } else {
                        driveTag.setInteger("amount", amount);
                    }

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
}
