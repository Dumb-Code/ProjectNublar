package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerGui;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerInputsGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
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

    public static final double TOTAL_AMOUNT = 16D;

    @Getter @Setter private double sugarAmount;
    @Getter @Setter private double boneAmount;
    @Getter @Setter private double plantAmount;

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

        this.sugarAmount = compound.getDouble("SugarAmount");
        this.boneAmount = compound.getDouble("BoneAmount");
        this.plantAmount = compound.getDouble("PlantAmount");

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

        compound.setDouble("SugarAmount", this.sugarAmount);
        compound.setDouble("BoneAmount", this.boneAmount);
        compound.setDouble("PlantAmount", this.plantAmount);

        return super.writeToNBT(compound);
    }

    @Override
    protected int getInventorySize() {
        return 9;
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
        return Lists.newArrayList(new MachineProcess<>(this, new int[]{7}, new int[]{8}));
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        switch (slot) {
            case 0: return stack.getItem() == ItemHandler.HARD_DRIVE;
            case 1: return MachineUtils.getWaterAmount(stack) != -1;
            case 2: return MachineUtils.getSugarMatter(stack) > 0;
            case 3: return MachineUtils.getBoneMatter(stack) > 0;
            case 4: return MachineUtils.getPlantMatter(stack, this.world, this.pos) > 0;
            case 5: return stack.getItem() instanceof DriveUtils.DriveInformation && ((DriveUtils.DriveInformation) stack.getItem()).hasInformation(stack);
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

        NBTTagCompound testNbt = this.handler.getStackInSlot(0).getOrCreateSubCompound(ProjectNublar.MODID).getCompoundTag("drive_information");
        if((!this.selectOneKey.isEmpty() && !testNbt.hasKey(this.selectOneKey, Constants.NBT.TAG_COMPOUND)) || this.selectOneAmount == 0 != this.selectOneKey.isEmpty()) {
            this.selectOneKey = "";
            this.selectOneAmount = 0D;
        }
        if((!this.selectTwoKey.isEmpty() && !testNbt.hasKey(this.selectTwoKey, Constants.NBT.TAG_COMPOUND)) || this.selectTwoAmount == 0 != this.selectTwoKey.isEmpty()) {
            this.selectTwoKey = "";
            this.selectTwoAmount = 0D;
        }
        if((!this.selectThreeKey.isEmpty() && !testNbt.hasKey(this.selectThreeKey, Constants.NBT.TAG_COMPOUND)) || this.selectThreeAmount == 0 != this.selectThreeKey.isEmpty()) {
            this.selectThreeKey = "";
            this.selectThreeAmount = 0D;
        }

        if(this.sugarAmount < TOTAL_AMOUNT) {
            this.sugarAmount = Math.min(TOTAL_AMOUNT, this.sugarAmount + MachineUtils.getSugarMatter(this.handler.getStackInSlot(2).splitStack(1)));
        }
        if(this.boneAmount < TOTAL_AMOUNT) {
            this.boneAmount = Math.min(TOTAL_AMOUNT, this.boneAmount + MachineUtils.getBoneMatter(this.handler.getStackInSlot(3).splitStack(1)));
        }
        if(this.plantAmount < TOTAL_AMOUNT) {
            this.plantAmount = Math.min(TOTAL_AMOUNT, this.plantAmount + MachineUtils.getPlantMatter(this.handler.getStackInSlot(4).splitStack(1), this.world, this.pos));
        }

        if(this.consumeTimer != 0) {
            if(!this.canConsume()) {
                this.consumeTimer = 0;
            } else if(this.consumeTimer++ >= 30) {
                ItemStack inStack = this.handler.getStackInSlot(5);
                ItemStack out = inStack.splitStack(1);

                if(!out.isEmpty() && !this.world.isRemote) {
                    DriveUtils.addItemToDrive(this.handler.getStackInSlot(0), out);
                }

                if(out.getItem() instanceof DriveUtils.DriveInformation) {
                    ItemStack outItem = ((DriveUtils.DriveInformation) out.getItem()).getOutItem(out);
                    this.handler.insertOutputItem(6, outItem, false);
                }
                this.consumeTimer = 1;
            }
        } else if(this.canConsume()) {
            this.consumeTimer = 1;
        }
    }

    private boolean canConsume() {
        ItemStack in = this.handler.getStackInSlot(5);
        if(in.getItem() instanceof DriveUtils.DriveInformation && this.handler.getStackInSlot(0).getItem() == ItemHandler.HARD_DRIVE && DriveUtils.canAdd(this.handler.getStackInSlot(0), in)) {
            ItemStack out = ((DriveUtils.DriveInformation) in.getItem()).getOutItem(in);
            return out.isEmpty() || this.handler.insertOutputItem(6, out, true).isEmpty();
        }
        return false;
    }

    @Nonnull
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
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return tab != 0 ? new SequencingSynthesizerInputsGui(player, this, info, tab) : new SequencingSynthesizerGui(player, this, info, tab);
    }

    @Override
    protected void addTabs(List<TabInformationBar.Tab> tabList) {
        tabList.add(new DefaultTab(0));
        tabList.add(new DefaultTab(1));
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        boolean input = tab != 0;
        return input ?
                new MachineModuleContainer(player, 84, 176,
                        new MachineModuleSlot(this, 0, 78, 6) {
                            @Nullable
                            @Override
                            public String getSlotTexture() {
                                return ProjectNublar.MODID + ":items/hard_drive";
                            }
                        },
                        new MachineModuleSlot(this, 1, 21, 58),
                        new MachineModuleSlot(this, 2, 59, 58),
                        new MachineModuleSlot(this, 3, 97, 58),
                        new MachineModuleSlot(this, 4, 135, 58)
                ) :
                new MachineModuleContainer(player, 110, 208,
                        new MachineModuleSlot(this, 0, 187, 5) {
                            @Nullable
                            @Override
                            public String getSlotTexture() {
                                return ProjectNublar.MODID + ":items/hard_drive";
                            }
                        },
                        new MachineModuleSlot(this, 5, 60, 90),
                        new MachineModuleSlot(this, 6, 40, 90),
                        new MachineModuleSlot(this, 7, 118, 90),
                        new MachineModuleSlot(this, 8, 167, 90));
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
