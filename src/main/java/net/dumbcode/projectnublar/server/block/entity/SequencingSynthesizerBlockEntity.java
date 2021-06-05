package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.DnaEditingGui;
import net.dumbcode.projectnublar.client.gui.machines.SequencingGui;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerInputsGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.DriveItem;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class SequencingSynthesizerBlockEntity extends MachineModuleBlockEntity<SequencingSynthesizerBlockEntity> implements DyableBlockEntity {

    private final FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME, p -> p.getFluid() == Fluids.WATER) {

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    };

    private final LazyOptional<FluidTank> capability = LazyOptional.of(() -> this.tank);


    public static final double DEFAULT_STORAGE = 16D;

    public static final int TOTAL_SLOTS = 9;
    public static final int SLOTS_AT_50 = 3;
    private static final float SLOTS_GRADIENT = (TOTAL_SLOTS - SLOTS_AT_50) / 0.5F;
    private static final float SLOTS_OFFSET = 2*SLOTS_AT_50 - TOTAL_SLOTS;

    private static final int HDD_TOTAL_CONSUME_TIME = 200;
    private static final int SSD_TOTAL_CONSUME_TIME = HDD_TOTAL_CONSUME_TIME / 2;

    @Getter
    private double totalStorage = DEFAULT_STORAGE;

    @Getter @Setter private double sugarAmount;
    @Getter @Setter private double boneAmount;
    @Getter @Setter private double plantAmount;

    private int totalConsumeTime;
    private int consumeTimer;

    @Getter @Setter private DyeColor dye = DyeColor.BLACK;

//    private String selectOneKey = "";
//    private double selectOneAmount;
//
//    private String selectTwoKey = "";
//    private double selectTwoAmount;
//
//    private String selectThreeKey = "";
//    private double selectThreeAmount;

    private final SelectedDnaEntry[] selectedDNAs = new SelectedDnaEntry[TOTAL_SLOTS];

    public SequencingSynthesizerBlockEntity() {
        super(ProjectNublarBlockEntities.SEQUENCING_SYNTHESIZER.get());
        for (int i = 0; i < this.selectedDNAs.length; i++) {
            this.selectedDNAs[i] = new SelectedDnaEntry();
        }
    }

    @Override
    public void tiersUpdated() {
        super.tiersUpdated();
        float tanks = this.getTierModifier(MachineModuleType.TANKS, 0.5F);
        this.tank.setCapacity((int) (FluidAttributes.BUCKET_VOLUME * tanks));
        if(this.tank.getFluidAmount() > this.tank.getCapacity()) {
            this.tank.setFluid(new FluidStack(this.tank.getFluid(), this.tank.getCapacity()));
        }
        this.totalStorage = DEFAULT_STORAGE * tanks;

        this.sugarAmount = MathHelper.clamp(this.sugarAmount, 0, this.totalStorage);
        this.boneAmount = MathHelper.clamp(this.boneAmount, 0, this.totalStorage);
        this.plantAmount = MathHelper.clamp(this.plantAmount, 0, this.totalStorage);
        FluidStack fluid = this.tank.getFluid();
        if(!fluid.isEmpty()) {
            fluid.setAmount(MathHelper.clamp(fluid.getAmount(), 0, this.tank.getCapacity()));
        }
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
    public void load(BlockState state, CompoundNBT compound) {
        super.save(compound);
        this.tank.readFromNBT(compound.getCompound("FluidTank"));
        this.dye = DyeColor.byId(compound.getInt("Dye"));
        this.consumeTimer = compound.getInt("ConsumeTimer");

        ListNBT list = compound.getList("SelectedDnaList", 10);
        for (int i = 0; i < this.selectedDNAs.length; i++) {
            CompoundNBT tag = list.getCompound(i);
            this.selectedDNAs[i].setKey(tag.getString("Key"));
            this.selectedDNAs[i].setAmount(tag.getDouble("Amount"));
        }

        this.sugarAmount = compound.getDouble("SugarAmount");
        this.boneAmount = compound.getDouble("BoneAmount");
        this.plantAmount = compound.getDouble("PlantAmount");

    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("FluidTank", this.tank.writeToNBT(new CompoundNBT()));
        compound.putInt("Dye", this.dye.getId());
        compound.putInt("ConsumeTimer", this.consumeTimer);

        ListNBT list = new ListNBT();
        for (SelectedDnaEntry selectedDNA : this.selectedDNAs) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("Key", selectedDNA.getKey());
            tag.putDouble("Amount", selectedDNA.getAmount());
            list.add(tag);
        }
        compound.put("SelectedDnaList", list);

        compound.putDouble("SugarAmount", this.sugarAmount);
        compound.putDouble("BoneAmount", this.boneAmount);
        compound.putDouble("PlantAmount", this.plantAmount);

        return super.save(compound);
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
            case 0: return stack.getItem() instanceof DriveItem;
            case 1: return MachineUtils.getWaterAmount(stack) != -1;
            case 2: return MachineUtils.getSugarMatter(stack) > 0;
            case 3: return MachineUtils.getBoneMatter(stack) > 0;
            case 4: return MachineUtils.getPlantMatter(stack, this.level, this.worldPosition) > 0;
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
        if(slot == 0) {
            ItemStack stack = this.handler.getStackInSlot(slot);
            if(!stack.isEmpty() && stack.getItem() instanceof DriveItem) {
                this.totalConsumeTime = ((DriveItem) stack.getItem()).isSsd() ? SSD_TOTAL_CONSUME_TIME : HDD_TOTAL_CONSUME_TIME;
            }
        }
        super.onSlotChanged(slot);
    }

    @Override
    public void tick() {
        super.tick();

        CompoundNBT testNbt = this.handler.getStackInSlot(0).getOrCreateTagElement(ProjectNublar.MODID).getCompound("drive_information");
        for (SelectedDnaEntry dna : this.selectedDNAs) {
            if((!dna.getKey().isEmpty() && !testNbt.contains(dna.getKey(), Constants.NBT.TAG_COMPOUND)) || dna.getAmount() == 0 != dna.getKey().isEmpty()) {
                dna.setKey("");
                dna.setAmount(0);
            }
        }

        this.sugarAmount = this.updateAmount(this.sugarAmount, this.handler.getStackInSlot(2), MachineUtils::getSugarMatter);
        this.boneAmount = this.updateAmount(this.boneAmount, this.handler.getStackInSlot(3), MachineUtils::getBoneMatter);
        this.plantAmount = this.updateAmount(this.plantAmount, this.handler.getStackInSlot(4), stack -> MachineUtils.getPlantMatter(stack, this.level, this.worldPosition));

        if(this.consumeTimer != 0) {
            if(!this.canConsume()) {
                this.consumeTimer = 0;
            } else if(this.consumeTimer++ >= this.totalConsumeTime) {
                ItemStack inStack = this.handler.getStackInSlot(5);
                ItemStack out = inStack.split(1);

                if(!out.isEmpty() && !this.level.isClientSide) {
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

    @Override
    public int[] constantInputSlots() {
        return new int[] { 0, 1, 2, 3, 4, 5 };
    }

    private double updateAmount(double currentAmount, ItemStack stack, ToDoubleFunction<ItemStack> amountGetter) {
        if(currentAmount < this.totalStorage && !stack.isEmpty()) {
            double amount = amountGetter.applyAsDouble(stack);
            if(amount > 0) {
                stack.shrink(1);
                return Math.min(this.totalStorage, currentAmount + amount);
            }
        }
        return currentAmount;
    }

    private boolean canConsume() {
        ItemStack in = this.handler.getStackInSlot(5);
        if(in.getItem() instanceof DriveUtils.DriveInformation && this.handler.getStackInSlot(0).getItem() instanceof DriveItem && DriveUtils.canAdd(this.handler.getStackInSlot(0), in)) {
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
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            this.selectedDNAs[ID].setKey(key);
            this.selectedDNAs[ID].setAmount(amount);
        }
    }

    public double getSelectAmount(int ID) {
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            return this.selectedDNAs[ID].getAmount();
        }
        return 0;
    }

    public String getSelectKey(int ID) {
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            return this.selectedDNAs[ID].getKey();
        }
        return "";
    }

    @Override
    protected void addTabs(List<TabInformationBar.Tab> tabList) {
        tabList.add(new DefaultTab(0));
        tabList.add(new DefaultTab(1));
        tabList.add(new DefaultTab(2));
    }

    @Override
    public TabbedGuiContainer<MachineModuleContainer> createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        switch (tab) {
            case 0:
                return new SequencingGui(container, inventory, title, info);
            case 1:
                return new DnaEditingGui(this, container, inventory, title, info);
            default:
                return new SequencingSynthesizerInputsGui(this, container, title, info, inventory);
        }
    }

    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        switch (tab) {
            case 0:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, 110, 208,
                    new MachineModuleSlot(this, 0, 187, 5)
                    .setBackground(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(ProjectNublar.MODID, "items/hard_drive")),
                    new MachineModuleSlot(this, 5, 60, 90),
                    new MachineModuleSlot(this, 6, 40, 90),
                    new MachineModuleSlot(this, 7, 118, 90),
                    new MachineModuleSlot(this, 8, 167, 90)
                );
            case 1:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, -1, 208,
                    new MachineModuleSlot(this, 0, 187, 5)
                        .setBackground(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(ProjectNublar.MODID, "items/hard_drive"))
                    );
            default:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, 84, 176,
                    new MachineModuleSlot(this, 0, 78, 6)
                        .setBackground(PlayerContainer.BLOCK_ATLAS, new ResourceLocation(ProjectNublar.MODID, "items/hard_drive")),
                    new MachineModuleSlot(this, 1, 21, 58),
                    new MachineModuleSlot(this, 2, 59, 58),
                    new MachineModuleSlot(this, 3, 97, 58),
                    new MachineModuleSlot(this, 4, 135, 58)
                );
        }
    }

    @Override
    public ITextComponent createTitle(int tab) {
        switch (tab) {
            case 0:
                return new TranslationTextComponent(ProjectNublar.MODID + ".container.sequencing.title");
            case 1:
                return new TranslationTextComponent(ProjectNublar.MODID + ".container.dna_editing.title");
            default:
                return new TranslationTextComponent(ProjectNublar.MODID + ".container.sequencing_synthesizing.title");

        }
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

    @Data
    public static class SelectedDnaEntry {
        private String key = "";
        private double amount;
    }

    public static int getSlots(float percentage) {
        return MathHelper.floor(MathHelper.clamp(percentage, 0.5, 1.0)*SLOTS_GRADIENT + SLOTS_OFFSET);
    }

    public static double getPercentageForSlot(int slots) {
        return Math.round(MathHelper.clamp((slots - SLOTS_OFFSET) / SLOTS_GRADIENT, 0.5, 1) * 100D);
    }

}
