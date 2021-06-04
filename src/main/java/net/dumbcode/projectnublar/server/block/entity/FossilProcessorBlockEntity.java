package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.FossilProcessorGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.FilterItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.recipes.FossilProcessorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FossilProcessorBlockEntity extends MachineModuleBlockEntity<FossilProcessorBlockEntity> {

    private static final int DEFAULT_CAPACITY = FluidAttributes.BUCKET_VOLUME * 2;
    private final FluidTank tank = new FluidTank(DEFAULT_CAPACITY, s -> s.getFluid() == Fluids.WATER) {

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final LazyOptional<FluidTank> capability = LazyOptional.of(() -> this.tank);

    public FossilProcessorBlockEntity() {
        super(ProjectNublarBlockEntities.FOSSIL_PROCESSOR.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return this.capability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tiersUpdated() {
        this.tank.setCapacity((int) (DEFAULT_CAPACITY * this.getTierModifier(MachineModuleType.TANKS, 0.5F)));
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.tank.readFromNBT(compound.getCompound("FluidTank"));

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("FluidTank", this.tank.writeToNBT(new CompoundNBT()));

        return super.save(compound);
    }

    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        switch (slot) {
            case 0: return MachineUtils.getWaterAmount(stack) != -1;
            case 2: return stack.getItem() == ItemHandler.EMPTY_TEST_TUBE.get();
            case 3: return stack.getItem() instanceof FilterItem;
        }
        return super.isItemValidFor(slot, stack);
    }

    @Override
    public int slotSize(int slot) {
        if(slot == 3) { //Filter
            return 1;
        }
        return super.slotSize(slot);
    }

    @Override
    public int[] constantInputSlots() {
        return new int[] { 0, 2, 3 };
    }

    private int layer;

    @Override
    protected void onSlotChanged(int slot) {
        if(slot == 0 && this.layer == 0) {
            this.layer++;
            this.handler.setStackInSlot(slot, MachineUtils.fillTank(this.handler.getStackInSlot(slot), this.tank));
            this.layer--;
        }
        super.onSlotChanged(slot);
    }

    @Override
    protected List<MachineRecipe<FossilProcessorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
                FossilProcessorRecipe.INSTANCE
        );
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    protected FossilProcessorBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<FossilProcessorBlockEntity>> createProcessList() {
        return Lists.newArrayList(new MachineProcess<>(this, new int[]{1}, new int[]{4}));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TabbedGuiContainer<MachineModuleContainer> createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        return new FossilProcessorGui(container, inventory, title, info, this.tank);
    }

    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        return new MachineModuleContainer(windowId, this, player.inventory, tab, 83, 176,
            new MachineModuleSlot(this, 0, 8, 61), //water
            new MachineModuleSlot(this, 1, 67, 12), //fossil
            new MachineModuleSlot(this, 2, 67, 52), //test tub
            new MachineModuleSlot(this, 3, 67, 32), //Filter
            new MachineModuleSlot(this, 4, 126, 32) //output
        );
    }

    @Override
    public ITextComponent createTitle(int tab) {
        return new TranslationTextComponent(ProjectNublar.MODID + ".containers.fossil_processor.title");
    }

    // TODO: Change for balance, values are just for testing
    @Override
    public int getBaseEnergyProduction() {
        return 0;
    }

    @Override
    public int getBaseEnergyConsumption() {
        return 1;
    }

    @Override
    public int getEnergyCapacity() {
        return 1000;
    }

    @Override
    public int getEnergyMaxTransferSpeed() {
        return 50;
    }

    @Override
    public int getEnergyMaxExtractSpeed() {
        return 0;
    }

}
