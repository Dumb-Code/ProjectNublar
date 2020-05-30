package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.network.C18OpenContainer;
import net.dumbcode.projectnublar.server.network.S42SyncMachineProcesses;
import net.dumbcode.projectnublar.server.network.S43SyncMachineStack;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class MachineModuleBlockEntity<B extends MachineModuleBlockEntity<B>> extends SimpleBlockEntity implements ITickable {

    public final Collection<MachineRecipe<B>> recipes = Collections.unmodifiableCollection(this.getAllRecipes());
    private final B asB = asB();

    @Getter protected final MachineModuleItemStackHandler<B> handler = new MachineModuleItemStackHandler<>(this, this.getInventorySize());
    private final List<MachineProcess<B>> processes = this.createProcessList();

    private final MachineModuleItemStackWrapper inputWrapper;
    private final MachineModuleItemStackWrapper outputWrapper;

    protected final Map<MachineModuleType, Integer> machineStateMap = new HashMap<>();

    @Setter private boolean positionDirty;

    @Getter
    private EnergyStorage energy;

    public MachineModuleBlockEntity() {
        this.energy = new EnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), getEnergyMaxExtractSpeed());
        this.inputWrapper = this.getFromProcesses(MachineProcess::getInputSlots, this.constantInputSlots());
        this.outputWrapper = this.getFromProcesses(MachineProcess::getOutputSlots, this.constantOutputSlots());
    }

    private MachineModuleItemStackWrapper getFromProcesses(Function<MachineProcess<B>, int[]> func, int[] constantSlots) {
        return new MachineModuleItemStackWrapper(this.handler,
            IntStream.concat(
                this.processes.stream()
                    .map(func)
                    .flatMapToInt(Arrays::stream),
                Arrays.stream(constantSlots)
            ).toArray()
        );
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("ItemHandler", this.handler.serializeNBT());
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

        NBTTagCompound stateNBT = new NBTTagCompound();
        this.machineStateMap.forEach((type, part) -> stateNBT.setInteger(type.getName(), part));
        compound.setTag("MachineState", stateNBT);

        NBTTagCompound energyNBT = new NBTTagCompound();
        energyNBT.setInteger("Amount", energy.getEnergyStored());
        compound.setTag("Energy", energyNBT);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.handler.deserializeNBT(compound.getCompoundTag("ItemHandler"));
        for (int i = 0; i < compound.getInteger("ProcessCount"); i++) {
            MachineProcess<B> process = this.processes.get(i);
            NBTTagCompound nbt = compound.getCompoundTag("Process_" + i);
            process.setTime(nbt.getInteger("Time"));
            process.setTotalTime(nbt.getInteger("TotalTime"));
            process.setCurrentRecipe(nbt.hasKey("Recipe", Constants.NBT.TAG_STRING) ? this.getRecipe(new ResourceLocation(nbt.getString("Recipe"))) : null);
            process.setProcessing(nbt.getBoolean("Processing")); //Is this really needed?
        }

        this.machineStateMap.clear();
        NBTTagCompound stateNBT = compound.getCompoundTag("MachineState");
        for (String s : stateNBT.getKeySet()) {
            this.machineStateMap.put(new MachineModuleType(s), stateNBT.getInteger(s));
        }
        this.tiersUpdated();

        NBTTagCompound energyNBT = compound.getCompoundTag("Energy");
        energy = new EnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), getEnergyMaxExtractSpeed(), energyNBT.getInteger("Amount"));
    }

    @Override
    public void update() {
        if(!this.world.isRemote) {
            updateEnergyNetwork();
            boolean hasPower = energy.extractEnergy(getBaseEnergyConsumption(), false) >= getBaseEnergyConsumption();
            for (MachineProcess<B> process : this.processes) {
                if (!canProvideEnergyForProcess(process)) {
                    process.setHasPower(false);
                    this.getInterruptAction(process).processConsumer.accept(process);
                    continue;
                }
                process.setHasPower(true);
                if (hasPower && this.canProcess(process) && (process.currentRecipe == null || process.currentRecipe.accepts(this.asB, process))) {
                    if (process.isProcessing() || this.searchForRecipes(process)) {
                        if (process.isFinished()) {
                            MachineRecipe<B> recipe = process.getCurrentRecipe();
                            if (recipe != null) {
                                recipe.onRecipeFinished(this.asB, process);
                                process.setTime(0);
                                if (!recipe.accepts(this.asB, process)) {
                                    process.setProcessing(false);
                                    process.setTotalTime(0);
                                    process.setTime(0);
                                    process.setCurrentRecipe(null);
                                    this.searchForRecipes(process);
                                } else {
                                    recipe.onRecipeStarted(asB(), process);
                                }
                            } else {
                                ProjectNublar.getLogger().error("Unable to find recipe " + process.getCurrentRecipe() + " as it does not exist.");
                            }
                        } else {
                            energy.extractEnergy(process.getCurrentConsumptionPerTick(), false); // consume energy for process
                            energy.receiveEnergy(process.getCurrentProductionPerTick(), false);
                            process.tick();
                        }
                        this.markDirty();
                    }
                } else if (process.isProcessing()) {
                    this.getInterruptAction(process).processConsumer.accept(process);
                }
            }
            //todo: only sync when needed
            ProjectNublar.NETWORK.sendToDimension(new S42SyncMachineProcesses(this), this.world.provider.getDimension());
        } else {
            for (MachineProcess<B> process : this.processes) {
                if(process.isProcessing() && !process.isFinished()) {
                    process.setTime(process.getTime() + 1);
                }
            }
        }
    }

    private boolean canProvideEnergyForProcess(MachineProcess<B> process) {
        int amountToProvide = process.getCurrentConsumptionPerTick();
        return energy.extractEnergy(amountToProvide, true) >= amountToProvide;
    }

    public int getTier(MachineModuleType type) {
        return this.machineStateMap.getOrDefault(type, 0);
    }

    public void setTier(MachineModuleType type, int tier) {
        this.machineStateMap.put(type, tier);
        this.tiersUpdated();
    }

    public float getTierModifier(MachineModuleType type, float step) {
        return this.getTier(type)*step + 1;
    }

    public void tiersUpdated() {
    }

    public int[] constantInputSlots() {
        return new int[0];
    }

    public int[] constantOutputSlots() {
        return new int[0];
    }

    /**
     * How much does this machine produce right now ? (in RF/FE per tick)
     * @return
     */
    public abstract int getBaseEnergyProduction();

    /**
     * How much energy this machine consumes by itself (in RF/FE per tick) ? (not accounting for processes)
     */
    public abstract int getBaseEnergyConsumption();

    public abstract int getEnergyCapacity();

    /**
     * in RF/FE per tick
     */
    public abstract int getEnergyMaxTransferSpeed();

    /**
     * in RF/FE per tick
     */
    public abstract int getEnergyMaxExtractSpeed();

    public int getEnergyToSendToNeighbor(IEnergyStorage storage, int neighborCount) {
        return getEnergyMaxExtractSpeed()/neighborCount;
    }

    private List<IEnergyStorage> getEnergyNeighbors() {
        List<IEnergyStorage> result = new LinkedList<>();
        for(EnumFacing facing : EnumFacing.values()) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if(te != null && te.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                result.add(te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()));
            }
        }
        return result;
    }

    /**
     * Send energy to neighbors that accept it
     */
    private void updateEnergyNetwork() {
        energy.receiveEnergy(getBaseEnergyProduction(), false); // produce & store energy if able
        List<IEnergyStorage> neighboringEnergyInterfaces = getEnergyNeighbors();
        int neighborCount = neighboringEnergyInterfaces.size();
        neighboringEnergyInterfaces.sort(Comparator.comparing(IEnergyStorage::getEnergyStored));
        for(IEnergyStorage neighbor : neighboringEnergyInterfaces) {
            // Send some type of your energy to the neighbor
            int energyToSend = getEnergyToSendToNeighbor(neighbor, neighborCount);
            int maxEnergyAbleToSend = energy.extractEnergy(energyToSend, true);
            int maxEnergyAbleToReceive = neighbor.receiveEnergy(maxEnergyAbleToSend, true);
            neighbor.receiveEnergy(maxEnergyAbleToReceive, false);
            energy.extractEnergy(maxEnergyAbleToReceive, false);

            // Extract as much energy as possible from that neighbor
//            int maxExtractable = neighbor.extractEnergy(Integer.MAX_VALUE, true);
//            int maxReceivable = energy.receiveEnergy(maxExtractable, true);
//           //  actual transfer
//            energy.receiveEnergy(neighbor.extractEnergy(maxReceivable, false), false);
        }
    }

    @Nullable
    public MachineProcess<B> getProcessFromSlot(int slot) {
        for (MachineProcess<B> process : this.processes) {
            for (int i : process.getInputSlots()) {
                if(i == slot) {
                    return process;
                }
            }
        }
        return null;
    }

    public int getProcessCount() {
        return this.processes.size();
    }

    public MachineProcess<B> getProcess(int id) {
        return this.processes.get(id);
    }

    public boolean searchForRecipes(MachineProcess<B> process) {
        if(!this.world.isRemote) {
            for (MachineRecipe<B> recipe : this.recipes) {
                if(recipe.accepts(this.asB, process) && this.canProcess(process)) {
                    process.setCurrentRecipe(recipe);
                    if(!process.isProcessing()) {
                        recipe.onRecipeStarted(asB(), process);
                    }
                    process.setProcessing(true);
                    process.setTotalTime(recipe.getRecipeTime(this.asB, process));
                    this.markDirty();
                    return true;
                }
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

    public void dropEmStacks() {
        for (int i = 0; i < this.handler.getSlots(); i++) {
            InventoryHelper.spawnItemStack(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.handler.getStackInSlot(i));
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY
                || super.hasCapability(capability, facing);
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
        if(capability == CapabilityEnergy.ENERGY) {
            return (T) energy;
        }
        return super.getCapability(capability, facing);
    }

    protected boolean canProcess(MachineProcess process) {
        return true;
    }

    protected ProcessInterruptAction getInterruptAction(MachineProcess process) {
        return ProcessInterruptAction.RESET;
    }

    protected void onSlotChanged(int slot) {
        if(!this.world.isRemote) {
            ProjectNublar.NETWORK.sendToDimension(new S43SyncMachineStack(this, slot), this.world.provider.getDimension());
        }
    }

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


    public TabInformationBar createInfo() {
        return new TabInformationBar(this::createTabList);
    }

    private List<TabInformationBar.Tab> createTabList() {
        List<TabInformationBar.Tab> tabs = Lists.newArrayList();
        for (MachineModuleBlockEntity blockEntity : this.getSurroundings(Lists.newArrayList())) {
            blockEntity.addTabs(tabs);
        }
        return tabs;
    }

    private List<MachineModuleBlockEntity> getSurroundings(List<MachineModuleBlockEntity> list) {
        list.add(this);
        for (EnumFacing facing : EnumFacing.values()) {
            TileEntity te = world.getTileEntity(this.pos.offset(facing));
            if (te instanceof MachineModuleBlockEntity && !list.contains(te)) {
                ((MachineModuleBlockEntity)te).getSurroundings(list);
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

    protected void addTabs(List<TabInformationBar.Tab> tabList) {
        tabList.add(new DefaultTab(0));
    }

    //tab - used to split the same gui into diffrent tabs. Not used for grouping diffrent guis together with tabs
    @SideOnly(Side.CLIENT)
    public abstract GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab);

    public abstract Container createContainer(EntityPlayer player, int tab);

    @Setter
    @Getter
    public static class MachineProcess<B extends MachineModuleBlockEntity<B>> {
        private final int[] inputSlots;
        private final int[] outputSlots;

        private final int[] allSlots;
        private final MachineModuleBlockEntity<B> machine;

        private int time;
        private int totalTime;
        @Nullable
        MachineRecipe<B> currentRecipe;
        boolean processing;
        boolean hasPower;

        public MachineProcess(MachineModuleBlockEntity<B> machine, int[] inputSlots, int[] outputSlots) {
            this.inputSlots = inputSlots;
            this.outputSlots = outputSlots;
            this.machine = machine;
            this.allSlots = ArrayUtils.addAll(this.inputSlots, this.outputSlots);
        }

        public int getInputSlot(int index) {
            return this.inputSlots[index];
        }

        public int getOutputSlot(int index) {
            return this.outputSlots[index];
        }

        public int getCurrentConsumptionPerTick() {
            if(isFinished())
                return 0;
            if(currentRecipe != null)
                return currentRecipe.getCurrentConsumptionPerTick(machine.asB, this);
            return 0;
        }

        public int getCurrentProductionPerTick() {
            if(isFinished())
                return 0;
            if(currentRecipe != null)
                return currentRecipe.getCurrentProductionPerTick(machine.asB, this);
            return 0;
        }

        public void tick() {
            this.time++;
            if(currentRecipe != null) {
                this.totalTime = currentRecipe.getRecipeTime(machine.asB, this);
                currentRecipe.onRecipeTick(machine.asB, this);
            }
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

    protected class DefaultTab implements TabInformationBar.Tab {

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
            TabInformationBar info;
            if(Minecraft.getMinecraft().currentScreen instanceof TabbedGuiContainer) {
                info = ((TabbedGuiContainer) Minecraft.getMinecraft().currentScreen).getInfo();
            } else {
                info = MachineModuleBlockEntity.this.createInfo();
            }
            Minecraft.getMinecraft().displayGuiScreen(MachineModuleBlockEntity.this.createScreen(Minecraft.getMinecraft().player, info, this.tab));
            ProjectNublar.NETWORK.sendToServer(new C18OpenContainer(this.tab, MachineModuleBlockEntity.this.pos));
        }
    }
}
