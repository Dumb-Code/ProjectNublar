package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.FossilProcessorGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.recipes.FossilProcessorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FossilProcessorBlockEntity extends MachineModuleBlockEntity<FossilProcessorBlockEntity> {

    private final FluidTank tank = new FluidTank(Fluid.BUCKET_VOLUME * 5) {

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
            case 0: return getWaterAmount(stack) != -1;
            case 1: return true;
            case 2: return stack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
            case 3: return stack.getItem() == ItemHandler.FILTER;
        }
        return false;
    }

    @Override
    protected void onSlotChanged(int slot) {
        if(slot == 0) {
            ItemStack stack = this.handler.getStackInSlot(slot);
            if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,  null);
                if(fluidHandler != null) {
                    this.tank.fill(fluidHandler.drain(this.tank.getCapacity() - this.tank.getFluidAmount(), true), true);
                }
            } if(this.tank.getFluidAmount() < this.tank.getCapacity()) {
                if(stack.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER) {
                    this.tank.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME / 3), true);
                    this.handler.setStackInSlot(slot, new ItemStack(Items.GLASS_BOTTLE));
                } else {
                    int waterAmount = getWaterAmount(stack);
                    if(waterAmount != -1) {
                        FluidBucketWrapper wrapper = new FluidBucketWrapper(stack);
                        FluidStack drained = wrapper.drain(waterAmount, true);
                        if(drained != null) {
                            this.tank.fill(drained, true);
                        } else {
                            ProjectNublar.getLogger().warn("Tried to drain item {}, but yielded no results", stack.getItem().getRegistryName());
                        }
                        this.handler.setStackInSlot(slot, wrapper.getContainer());
                    }
                }
            }
        }
        super.onSlotChanged(slot);
    }

    //returns -1 if not a water item
    private static int getWaterAmount(ItemStack stack) {
        if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,  null);
            if(fluidHandler != null) {
                int totalAmount = -1;
                for (IFluidTankProperties properties : fluidHandler.getTankProperties()) {
                    FluidStack contents = properties.getContents();
                    if(contents != null && contents.getFluid() == FluidRegistry.WATER && contents.amount > 0 && properties.canDrainFluidType(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME))) {
                        totalAmount += properties.getContents().amount;
                    }
                }
                if(totalAmount != -1) {
                    return totalAmount;
                }
            }
        }
        if(stack.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER) {
            return Fluid.BUCKET_VOLUME / 3;
        }
        FluidStack fluid =  new FluidBucketWrapper(stack).getFluid();
        return fluid != null && fluid.getFluid() == FluidRegistry.WATER ? fluid.amount : -1;
    }

    @Override
    protected List<MachineRecipe<FossilProcessorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(new FossilProcessorRecipe(
                new ResourceLocation(ProjectNublar.MODID, "test"),
                stack -> stack.getItem() == Items.COOKIE,
                stack -> new ItemStack(Items.FLINT), 20));
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
        return Lists.newArrayList(new MachineProcess<>(new int[]{1}, new int[]{4}));
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player) {
        return new FossilProcessorGui(player, this);
    }

    @Override
    public Container createContainer(EntityPlayer player) {
        return new MachineModuleContainer(player, 138,
                new MachineModuleSlot(this, 0, 8, 116), //water
                new MachineModuleSlot(this, 1, 100, 50), //fossil
                new MachineModuleSlot(this, 2, 150, 50), //test tub
                new MachineModuleSlot(this, 3, 100, 100), //Filter
                new MachineModuleSlot(this, 4, 150, 100)); //output
    }
}
