package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.tab.TabListInformation;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C16DisplayTabbedGui;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class MachineModuleBlockEntity<B extends MachineModuleBlockEntity<B>> extends SimpleBlockEntity implements ITickable {

    public final Collection<MachineRecipe<B>> recipes = Collections.unmodifiableCollection(this.getAllRecipes());
    private final B asB = asB();

    @Getter protected final MachineModuleItemStackHandler handler = new MachineModuleItemStackHandler<>(this, this.getInventorySize());
    private final List<MachineProcess<B>> processes = this.createProcessList();

    private final MachineModuleItemStackWrapper inputWrapper;
    private final MachineModuleItemStackWrapper outputWrapper;

    @Getter @Setter private int stateID;

    public boolean positionDirty;

    public MachineModuleBlockEntity(){
        this.inputWrapper = this.getFromProcesses(MachineProcess::getInputSlots);
        this.outputWrapper = this.getFromProcesses(MachineProcess::getOutputSlots);
    }

    private MachineModuleItemStackWrapper getFromProcesses(Function<MachineProcess<B>, int[]> func) {
        List<Integer> list = Lists.newArrayList();
        for (MachineProcess<B> process : this.processes) {
            for (int i : func.apply(process)) {
                list.add(i);
            }
        }
        int[] aint = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            aint[i] = list.get(i);
        }
        return new MachineModuleItemStackWrapper(this.handler, aint);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("ItemHandler", this.handler.serializeNBT());
        compound.setInteger("StateID", this.stateID);
        compound.setInteger("ProcessCount", this.processes.size());
        for (int i = 0; i < this.processes.size(); i++) {
            MachineProcess process = this.processes.get(i);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("Time", process.time);
            nbt.setInteger("TotalTime", process.totalTime);
            if(process.currentRecipe != null) {
                nbt.setString("Recipe", process.currentRecipe.getRegistryName().toString());
            }
            nbt.setBoolean("Processing", process.processing); //Is this really needed?
            compound.setTag("Process_" + i, nbt);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.handler.deserializeNBT(compound.getCompoundTag("ItemHandler"));
        this.stateID = compound.getInteger("StateID");
        for (int i = 0; i < compound.getInteger("ProcessCount"); i++) {
            MachineProcess<B> process = this.processes.get(i);
            NBTTagCompound nbt = compound.getCompoundTag("Process_" + i);
            process.setTime(nbt.getInteger("Time"));
            process.setTotalTime(nbt.getInteger("TotalTime"));
            process.setCurrentRecipe(nbt.hasKey("Recipe", Constants.NBT.TAG_STRING) ? this.getRecipe(new ResourceLocation(nbt.getString("Recipe"))) : null);
            process.setProcessing(nbt.getBoolean("Processing")); //Is this really needed?
        }
    }

    @Override
    public void update() {
        for (MachineProcess<B> process : this.processes) {
            if(this.canProcess(process) && (process.currentRecipe == null || process.currentRecipe.accepts(this.asB, process))) {
                if(process.isProcessing() || this.searchForRecipes(process)) {
                    if(process.isFinished()) {
                        MachineRecipe<B> recipe = process.getCurrentRecipe();
                        if(recipe != null) {
                            recipe.onRecipeFinished(this.asB, process);
                            process.setTime(0);
                            if(!recipe.accepts(this.asB, process)) {
                                process.setProcessing(false);
                                process.setCurrentRecipe(null);
                                this.searchForRecipes(process);
                            }
                        } else {
                            ProjectNublar.getLogger().error("Unable to find recipe " + process.getCurrentRecipe() + " as it does not exist.");
                        }
                    } else {
                        process.tick();
                    }
                    this.markDirty();
                }
            } else if(process.isProcessing()) {
                this.getInterruptAction(process).processConsumer.accept(process);
            }
        }
    }

    @Nullable
    public MachineProcess<B> getProcess(int slot) {
        for (MachineProcess<B> process : this.processes) {
            for (int i : process.getInputSlots()) {
                if(i == slot) {
                    return process;
                }
            }
        }
        return null;
    }

    public boolean searchForRecipes(MachineProcess<B> process) {
        for (MachineRecipe<B> recipe : this.recipes) {
            if(recipe.accepts(this.asB, process) && this.canProcess(process)) {
                process.setProcessing(true);
                process.setCurrentRecipe(recipe);
                process.setTotalTime(recipe.getRecipeTime(this.asB, process));
                this.markDirty();
                return true;
            }
        }
        return false;
    }

    @Nullable
    public MachineRecipe<B> getRecipe(ResourceLocation location) {
        for (MachineRecipe<B> recipe : this.recipes) {
            if(recipe.getRegistryName().equals(location)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(facing == EnumFacing.DOWN) {
                return (T) this.outputWrapper;
            } else {
                return (T) this.inputWrapper;
            }
        }
        return super.getCapability(capability, facing);
    }

    protected boolean canProcess(MachineProcess process) {
        return true;
    }

    protected ProcessInterruptAction getInterruptAction(MachineProcess process) {
        return ProcessInterruptAction.RESET;
    }

    protected void onSlotChanged(int slot) {}

    protected abstract int getInventorySize();

    protected abstract List<MachineRecipe<B>> getAllRecipes();

    protected abstract B asB();

    protected abstract List<MachineProcess<B>> createProcessList();

    public int slotSize(int slot) {
        return 64;
    }

    public boolean isItemValidFor(int slot, ItemStack stack) {
        return this.isRecipeSlotValid(slot, stack);
    }

    protected boolean isRecipeSlotValid(int slot, ItemStack stack) {
        for (MachineProcess<B> process : this.processes) {
            for (int i = 0; i < process.getAllSlots().length; i++) {
                if(process.getAllSlots()[i] == slot) { //Get the process that contains the slot
                    if(process.getCurrentRecipe() != null) {
                        return process.getCurrentRecipe().acceptsInputSlot(this.asB, i, stack, process);
                    } else {
                        MachineRecipe<B> foundRecipe = null;
                        int totalFound = 0;
                        for (MachineRecipe<B> recipe : this.recipes) {
                            if(recipe.acceptsInputSlot(this.asB, i, stack, process)) {
                                totalFound++;
                                foundRecipe = recipe;
                            }
                        }
                        if(totalFound == 1) {
                            process.setCurrentRecipe(foundRecipe);
                        }
                        return foundRecipe != null;
                    }
                }
            }
        }
        return false; //Slot index was not an input. Log error?
    }


    public TabListInformation createInfo() {
        return new TabListInformation(this::createTabList);
    }

    private List<TabbedGui.Tab> createTabList() {
        List<TabbedGui.Tab> tabs = Lists.newArrayList();
        for (MachineModuleBlockEntity blockEntity : this.getSurroundings(Lists.newArrayList())) {
            blockEntity.addTabs(tabs);
        }
        return tabs;
    }

    private List<MachineModuleBlockEntity> getSurroundings(List<MachineModuleBlockEntity> list) {
        list.add(this);
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity te = world.getTileEntity(this.pos.offset(facing));
            if(te instanceof MachineModuleBlockEntity && !list.contains(te)) {
                ((MachineModuleBlockEntity) te).getSurroundings(list);
            }
        }
        return list;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.positionDirty = true;
    }

    @Override
    public void validate() {
        super.validate();
        this.positionDirty = true;
    }

    protected void addTabs(List<TabbedGui.Tab> tabList) {
        tabList.add(new DefaultTab(0));
    }

    //tab - used to split the same gui into diffrent tabs. Not used for grouping diffrent guis together with tabs
    @SideOnly(Side.CLIENT)
    public abstract GuiScreen createScreen(EntityPlayer player, TabListInformation info, int tab);

    public abstract Container createContainer(EntityPlayer player, int tab);

    @Getter
    @Setter
    public static class MachineProcess<B extends MachineModuleBlockEntity<B>> {
        final int[] inputSlots;
        final int[] outputSlots;

        final int[] allSlots;

        int time;
        int totalTime;
        @Nullable
        MachineRecipe<B> currentRecipe;
        boolean processing;

        public MachineProcess(int[] inputSlots, int[] outputSlots) {
            this.inputSlots = inputSlots;
            this.outputSlots = outputSlots;

            this.allSlots = ArrayUtils.addAll(this.inputSlots, this.outputSlots);
        }

        public void tick() {
            this.time++;
        }

        public boolean isFinished() {
            return this.time >= this.totalTime;
        }
    }

    protected enum ProcessInterruptAction {
        RESET(p -> p.setTime(0)),
        DECREASE(p -> p.setTime(p.getTime() - 1)),
        PAUSE(p -> {});

        private final Consumer<MachineProcess> processConsumer;

        ProcessInterruptAction(Consumer<MachineProcess> processConsumer) {
            this.processConsumer = processConsumer;
        }
    }

    protected class DefaultTab extends TabbedGui.Tab {

        private final int tab;

        public DefaultTab(int tab) {
            this.tab = tab;
        }

        @Override
        public boolean isDirty() {
            if(MachineModuleBlockEntity.this.positionDirty) {
                MachineModuleBlockEntity.this.positionDirty = false;
                return true;
            }
            return false;
        }

        @Override
        public void onClicked() {
            TabListInformation info;
            if(Minecraft.getMinecraft().currentScreen instanceof TabbedGui) {
                info = ((TabbedGui) Minecraft.getMinecraft().currentScreen).getInfo();
            } else {
                info = MachineModuleBlockEntity.this.createInfo();
            }
            Minecraft.getMinecraft().displayGuiScreen(MachineModuleBlockEntity.this.createScreen(Minecraft.getMinecraft().player, info, this.tab));
            ProjectNublar.NETWORK.sendToServer(new C16DisplayTabbedGui(MachineModuleBlockEntity.this.pos, this.tab));
        }
    }
}
