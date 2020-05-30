package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.FossilProcessorGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.FilterItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.recipes.FossilProcessorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FossilProcessorBlockEntity extends MachineModuleBlockEntity<FossilProcessorBlockEntity> {

    private static final int DEFAULT_CAPACITY = Fluid.BUCKET_VOLUME * 2;
    private final FluidTank tank = new FluidTank(DEFAULT_CAPACITY) {

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.getFluid() == FluidRegistry.WATER;
        }

        @Override
        public boolean canDrain() {
            return false;
        }
    };

    public FossilProcessorBlockEntity() {
        this.tank.setTileEntity(this);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) this.tank;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void tiersUpdated() {
        this.tank.setCapacity((int) (DEFAULT_CAPACITY * this.getTierModifier(MachineModuleType.TANKS, 0.5F)));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.tank.readFromNBT(compound.getCompoundTag("FluidTank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("FluidTank", this.tank.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }

    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        switch (slot) {
            case 0: return MachineUtils.getWaterAmount(stack) != -1;
            case 2: return stack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
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
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return new FossilProcessorGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(this, player, 83, 176,
                new MachineModuleSlot(this, 0, 8, 61), //water
                new MachineModuleSlot(this, 1, 67, 12), //fossil
                new MachineModuleSlot(this, 2, 67, 52), //test tub
                new MachineModuleSlot(this, 3, 67, 32), //Filter
                new MachineModuleSlot(this, 4, 126, 32)); //output
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
        return 50;
    }

}
