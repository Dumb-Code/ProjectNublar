package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.FossilProcessorScreen;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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

    private static final int DEFAULT_CAPACITY = FluidType.BUCKET_VOLUME * 2;
    private final MachineModuleFluidTank tank = new MachineModuleFluidTank(this, DEFAULT_CAPACITY, s -> s.getFluid() == Fluids.WATER).setCanDrain(false);

    private final LazyOptional<FluidTank> capability = LazyOptional.of(() -> this.tank);

    public FossilProcessorBlockEntity() {
        super(ProjectNublarBlockEntities.FOSSIL_PROCESSOR.get());
    }

    @Override
    protected int[] gatherExtraSyncData() {
        return new int[]{ this.tank.getCapacity(), this.tank.getFluidAmount() };
    }

    @Override
    public void onExtraSyncData(int[] aint) {
        this.tank.setCapacity(aint[0]);
        this.tank.setFluid(new FluidStack(Fluids.WATER, aint[1]));
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
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.tank.readFromNBT(compound.getCompound("FluidTank"));

    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.put("FluidTank", this.tank.writeToNBT(new CompoundTag()));

        return super.save(compound);
    }

    @Override
    protected int getInventorySize() {
        return 13;
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

    public MachineModuleFluidTank getTank() {
        return tank;
    }

    @Override
    protected FossilProcessorBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<FossilProcessorBlockEntity>> createProcessList() {
        return Lists.newArrayList(new MachineProcess<>(this, new int[]{1}, new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12}));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MachineContainerScreen createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        return new FossilProcessorScreen(container, inventory, title, info, this.tank, this.getProcess(0));
    }

    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        return new MachineModuleContainer(windowId, this, player.inventory, tab, 135, 176,
            new MachineModuleSlot(this, 0, 22, 35), //water
            new MachineModuleSlot(this, 1, 80, 13), //fossil
            new MachineModuleSlot(this, 2, 136, 35), //test tub
            new MachineModuleSlot(this, 3, 22, 14), //Filter

            //outputs
            new MachineModuleSlot(this, 4, 59, 61),
            new MachineModuleSlot(this, 5, 80, 61),
            new MachineModuleSlot(this, 6, 101, 61),
            new MachineModuleSlot(this, 7, 59, 82),
            new MachineModuleSlot(this, 8, 80, 82),
            new MachineModuleSlot(this, 9, 101, 82),
            new MachineModuleSlot(this, 10, 59, 103),
            new MachineModuleSlot(this, 11, 80, 103),
            new MachineModuleSlot(this, 12, 101, 103)
        );
    }

    @Override
    public ITextComponent createTitle(int tab) {
        return Component.translatable(ProjectNublar.MODID + ".containers.fossil_processor.title");
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
        return 5000;
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
