package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity>{
    INSTANCE;

    private final ResourceLocation registryName = new ResourceLocation(ProjectNublar.MODID, "dna_creation");

    private static ITextComponent createReason(String reason) {
        return ProjectNublar.translate("machine.sequencer.blocked.reason." + reason);
    }

    public static List<ITextComponent> getCannotStartReasons(SequencingSynthesizerBlockEntity blockEntity) {
        List<ITextComponent> list = new ArrayList<>();
        MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process = blockEntity.getProcess(1);

        if(process.getInputStack(0).getItem() != ItemHandler.EMPTY_TEST_TUBE.get()) {
            list.add(createReason("test_tube"));
        }

        if(blockEntity.getTank().getFluidAmount() < FluidAttributes.BUCKET_VOLUME / 2) {
            list.add(createReason("water"));
        }
        double size = SequencingSynthesizerBlockEntity.DEFAULT_STORAGE / 2;
        if(blockEntity.getPlantAmount() < size) {
            list.add(createReason("plant"));
        }
        if(blockEntity.getBoneAmount() < size) {
            list.add(createReason("bone"));
        }
        if(blockEntity.getSugarAmount() < size) {
            list.add(createReason("sugar"));
        }

        DinosaurHandler.getRegistry().containsKey(new ResourceLocation(blockEntity.getSelectKey(0)));

        double amount = 0;
        for (int i = 0; i < blockEntity.getSlots(); i++) {
            amount += blockEntity.getSelectAmount(i);
        }
        if(amount != 1) {
            list.add(createReason("dna"));
        }

        return list;
    }

    @Override
    public boolean accepts(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        return getCannotStartReasons(blockEntity).isEmpty();
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
        process.getInputStack(0).shrink(1);

        process.insertOutputItem(this.createStack(blockEntity), 0);


        double storageSize = SequencingSynthesizerBlockEntity.DEFAULT_STORAGE;

        blockEntity.getTank().drainInternal(FluidAttributes.BUCKET_VOLUME / 2, IFluidHandler.FluidAction.EXECUTE);
        blockEntity.setBoneAmount(blockEntity.getBoneAmount() - storageSize / 2);
        blockEntity.setPlantAmount(blockEntity.getPlantAmount() - storageSize / 2);
        blockEntity.setSugarAmount(blockEntity.getSugarAmount() - storageSize / 2);
        blockEntity.syncToClient();

    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process) {
        switch (slotIndex) {
            case 0: return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE.get();
        }
        return false;
    }

    private ItemStack createStack(SequencingSynthesizerBlockEntity blockEntity) {
        ResourceLocation location = new ResourceLocation(blockEntity.getSelectKey(0));
        if(DinosaurHandler.getRegistry().containsKey(location)) {
            ItemStack stack = new ItemStack(ItemHandler.TEST_TUBES_DNA.get(DinosaurHandler.getRegistry().getValue(location)));
            List<GeneticEntry<?, ?>> geneticEntries = blockEntity.gatherAllGeneticEntries();
            ListNBT collected = geneticEntries.stream()
                .map(g -> g.serialize(new CompoundNBT()))
                .collect(CollectorUtils.toNBTTagList());
            CompoundNBT nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
            nbt.put("Genetics", collected);

            if(blockEntity.getDinosaurGender().hasValue()) {
                nbt.putBoolean("IsMale", blockEntity.getDinosaurGender().getMale());
            }
            return stack;
        }
        ProjectNublar.getLogger().warn("Unable to complete recipe, {} was not a dinosaur", location);
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
