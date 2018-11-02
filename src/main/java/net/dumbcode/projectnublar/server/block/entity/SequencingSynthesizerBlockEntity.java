package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SequencingSynthesizerBlockEntity extends MachineModuleBlockEntity<SequencingSynthesizerBlockEntity> {

    private final FluidTank tank = new FluidTank(Fluid.BUCKET_VOLUME) {

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid.getFluid() == FluidRegistry.WATER;
        }

        @Override
        public boolean canDrain() {
            return false;
        }
    };

    private int consumeTimer;
    
    private String selectOneKey = "";
    private double selectOneAmount;

    private String selectTwoKey = "";
    private double selectTwoAmount;

    private String selectThreeKey = "";
    private double selectThreeAmount;
    
    public SequencingSynthesizerBlockEntity() {
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
        this.consumeTimer = compound.getInteger("ConsumeTimer");

        this.selectOneKey = compound.getString("SelectOneKey");
        this.selectOneAmount = compound.getDouble("SelectOneAmount");

        this.selectTwoKey = compound.getString("SelectTwoKey");
        this.selectTwoAmount = compound.getDouble("SelectTwoAmount");

        this.selectThreeKey = compound.getString("SelectThreeKey");
        this.selectThreeAmount = compound.getDouble("SelectThreeAmount");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("FluidTank", this.tank.writeToNBT(new NBTTagCompound()));
        compound.setInteger("ConsumeTimer", this.consumeTimer);

        compound.setString("SelectOneKey", this.selectOneKey);
        compound.setDouble("SelectOneAmount", this.selectOneAmount);

        compound.setString("SelectTwoKey", this.selectTwoKey);
        compound.setDouble("SelectTwoAmount", this.selectTwoAmount);

        compound.setString("SelectThreeKey", this.selectThreeKey);
        compound.setDouble("SelectThreeAmount", this.selectThreeAmount);
        
        return super.writeToNBT(compound);
    }

    @Override
    protected int getInventorySize() {
        return 7;
    }

    @Override
    protected List<MachineRecipe<SequencingSynthesizerBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
                SequencingSynthesizerRecipe.INSTANCE
        );
    }

    @Override
    protected SequencingSynthesizerBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<SequencingSynthesizerBlockEntity>> createProcessList() {
        return Lists.newArrayList(new MachineProcess<>(new int[]{4, 5}, new int[]{6}));
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        if(slot == 0) {
            return stack.getItem() == ItemHandler.STORAGE_DRIVE;
        } else if(slot == 1) {
            return MachineUtils.getWaterAmount(stack) != -1;
        } else if(slot == 2) {
            return stack.getItem() instanceof DriveUtils.DriveInformation && ((DriveUtils.DriveInformation) stack.getItem()).hasInformation(stack);
        }
        return super.isItemValidFor(slot, stack);
    }

    private int layer;

    @Override
    protected void onSlotChanged(int slot) {
        if(slot == 1 && this.layer == 0) {
            this.layer++;
            this.handler.setStackInSlot(slot, MachineUtils.fillTank(this.handler.getStackInSlot(slot), this.tank));
            this.layer--;
        }
        super.onSlotChanged(slot);
    }

    @Override
    public void update() {
        super.update();

        if(this.consumeTimer != 0) {
            if(!this.canConsume()) {
                this.consumeTimer = 0;
            } else if(this.consumeTimer++ >= 30) {
                ItemStack inStack = this.handler.getStackInSlot(2);
                ItemStack out = inStack.splitStack(1);

                if(!out.isEmpty() && !this.world.isRemote) {
                    DriveUtils.addItemToDrive(this.handler.getStackInSlot(0), out);
                }

                if(out.getItem() instanceof DriveUtils.DriveInformation) {
                    ItemStack outItem = ((DriveUtils.DriveInformation) out.getItem()).getOutItem(out);
                    this.handler.insertOutputItem(3, outItem, false);
                }
                this.consumeTimer = 1;
            }
        } else if(this.canConsume()) {
            this.consumeTimer = 1;
        }
    }

    private boolean canConsume() {
        ItemStack in = this.handler.getStackInSlot(2);
        if(in.getItem() instanceof DriveUtils.DriveInformation && this.handler.getStackInSlot(0).getItem() == ItemHandler.STORAGE_DRIVE && DriveUtils.canAdd(this.handler.getStackInSlot(0), in)) {
            ItemStack out = ((DriveUtils.DriveInformation) in.getItem()).getOutItem(in);
            return out.isEmpty() || this.handler.insertOutputItem(3, out, true).isEmpty();
        }
        return false;
    }

    public FluidTank getTank() {
        return this.tank;
    }
    
    public void setSelect(int ID, String key, double amount) {
        switch (ID) {
            case 1: 
                this.selectOneKey = key;
                this.selectOneAmount = amount;
                break;
            case 2:
                this.selectTwoKey = key;
                this.selectTwoAmount = amount;
                break;
            case 3:
                this.selectThreeKey = key;
                this.selectThreeAmount = amount;
                break;
        }
    }

    public double getSelectAmount(int ID) {
        switch (ID) {
            case 1: return this.selectOneAmount;
            case 2: return this.selectTwoAmount;
            case 3: return this.selectThreeAmount;
        }
        return 0;
    }

    public String getSelectKey(int ID) {
        switch (ID) {
            case 1: return this.selectOneKey;
            case 2: return this.selectTwoKey;
            case 3: return this.selectThreeKey;
        }
        return "";
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player) {
        return new SequencingSynthesizerGui(player, this);
    }

    @Override
    public Container createContainer(EntityPlayer player) {
        return new MachineModuleContainer(player, 136, 208,
                new MachineModuleSlot(this, 0, 187, 5) {
                    @Nullable
                    @Override
                    public String getSlotTexture() {
                        return ProjectNublar.MODID + ":items/storage_drive";
                    }
                },
                new MachineModuleSlot(this, 1, 5, 110),

                new MachineModuleSlot(this, 2, 65, 110),
                new MachineModuleSlot(this, 3, 47, 110),

                new MachineModuleSlot(this, 4, 97, 110),
                new MachineModuleSlot(this, 5, 118, 110),
                new MachineModuleSlot(this, 6, 167, 110)
        );
    }
}
