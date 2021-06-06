package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.network.C2SChangeContainerTab;
import net.dumbcode.projectnublar.server.network.S42SyncMachineProcesses;
import net.dumbcode.projectnublar.server.network.S43SyncMachineStack;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class MachineModuleBlockEntity<B extends MachineModuleBlockEntity<B>> extends SimpleBlockEntity implements ITickableTileEntity {

    public final Collection<MachineRecipe<B>> recipes = Collections.unmodifiableCollection(this.getAllRecipes());
    private final B asB = asB();

    @Getter protected final MachineModuleItemStackHandler<B> handler = new MachineModuleItemStackHandler<>(this, this.getInventorySize());
    private final List<MachineProcess<B>> processes = this.createProcessList();

    private final MachineModuleItemStackWrapper inputWrapper = this.getFromProcesses(MachineProcess::getInputSlots, this.constantInputSlots());
    private final MachineModuleItemStackWrapper outputWrapper = this.getFromProcesses(MachineProcess::getOutputSlots, this.constantOutputSlots());

    private final LazyOptional<IItemHandler> inputCapability = LazyOptional.of(() -> this.inputWrapper);
    private final LazyOptional<IItemHandler> outputCapability = LazyOptional.of(() -> this.outputWrapper);

    @Setter private boolean positionDirty;

    @Getter
    private final Set<UUID> openedUsers = new HashSet<>();

    @Getter
    private EnergyStorage energy;

    private final LazyOptional<EnergyStorage> energyCapability = LazyOptional.of(() -> this.energy);


    public MachineModuleBlockEntity(TileEntityType<?> type) {
        super(type);
        this.energy = new EnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), 50);//getEnergyMaxExtractSpeed()
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
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("ItemHandler", this.handler.serializeNBT());
        compound.putInt("ProcessCount", this.processes.size());
        for (int i = 0; i < this.processes.size(); i++) {
            MachineProcess<B> process = this.processes.get(i);
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Time", process.time);
            nbt.putInt("TotalTime", process.totalTime);
            if(process.currentRecipe != null) {
                nbt.putString("Recipe", process.currentRecipe.getRegistryName().toString());
            }
            nbt.putBoolean("Processing", process.processing); //Is this really needed?
            compound.put("Process_" + i, nbt);
        }

        CompoundNBT energyNBT = new CompoundNBT();
        energyNBT.putInt("Amount", energy.getEnergyStored());
        compound.put("Energy", energyNBT);
        return super.save(compound);
    }


    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.handler.deserializeNBT(compound.getCompound("ItemHandler"));
        for (int i = 0; i < compound.getInt("ProcessCount"); i++) {
            MachineProcess<B> process = this.processes.get(i);
            CompoundNBT nbt = compound.getCompound("Process_" + i);
            process.setTime(nbt.getInt("Time"));
            process.setTotalTime(nbt.getInt("TotalTime"));
            process.setCurrentRecipe(nbt.contains("Recipe", Constants.NBT.TAG_STRING) ? this.getRecipe(new ResourceLocation(nbt.getString("Recipe"))) : null);
            process.setProcessing(nbt.getBoolean("Processing")); //Is this really needed?
        }

        CompoundNBT energyNBT = compound.getCompound("Energy");
        energy = new EnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), getEnergyMaxExtractSpeed(), energyNBT.getInt("Amount"));
    }

    @Override
    public void tick() {
        if(this.level != null && !this.level.isClientSide) {
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
                        this.setChanged();
                    }
                } else if (process.isProcessing()) {
                    this.getInterruptAction(process).processConsumer.accept(process);
                }
            }
            //todo: only sync when needed
            ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(this.level::dimension), new S42SyncMachineProcesses(this));
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
        Block block = this.getBlockState().getBlock();
        if(!(block instanceof MachineModuleBlock)) {
           throw new IllegalStateException("Expected MachineModuleBlock, found " + block);
        }
        return this.getBlockState().getValue(((MachineModuleBlock) block).getPropertyMap().get(type));
    }

    @Override
    public void setChanged() {
        super.setChanged();
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
        for(Direction facing : Direction.values()) {
            TileEntity te = level.getBlockEntity(this.worldPosition.relative(facing));
            if(te != null) {
                te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).ifPresent(result::add);
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
        if(!this.level.isClientSide) {
            for (MachineRecipe<B> recipe : this.recipes) {
                if(recipe.accepts(this.asB, process) && this.canProcess(process)) {
                    process.setCurrentRecipe(recipe);
                    if(!process.isProcessing()) {
                        recipe.onRecipeStarted(asB(), process);
                    }
                    process.setProcessing(true);
                    process.setTotalTime(recipe.getRecipeTime(this.asB, process));
                    this.setChanged();
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
            InventoryHelper.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.handler.getStackInSlot(i));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if(side == Direction.DOWN) {
                return this.outputCapability.cast();
            }
            return this.inputCapability.cast();
        }
        if(cap == CapabilityEnergy.ENERGY) {
            return this.energyCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    protected boolean canProcess(MachineProcess process) {
        return true;
    }

    protected ProcessInterruptAction getInterruptAction(MachineProcess process) {
        return ProcessInterruptAction.RESET;
    }

    protected void onSlotChanged(int slot) {
        if(!this.level.isClientSide) {
            ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(this.level::dimension), new S43SyncMachineStack(this, slot));
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
        for (MachineModuleBlockEntity<?> blockEntity : this.getSurroundings(Lists.newArrayList())) {
            blockEntity.addTabs(tabs);
        }
        return tabs;
    }

    public List<MachineModuleBlockEntity<?>> getSurroundings(List<MachineModuleBlockEntity<?>> list) {
        list.add(this);
        for (Direction facing : Direction.values()) {
            TileEntity te = level.getBlockEntity(this.worldPosition.relative(facing));
            if (te instanceof MachineModuleBlockEntity && !list.contains(te)) {
                ((MachineModuleBlockEntity<?>)te).getSurroundings(list);
            }
        }
        return list;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.positionDirty = true;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        this.positionDirty = true;
    }

    protected void addTabs(List<TabInformationBar.Tab> tabList) {
        tabList.add(new DefaultTab(0));
    }

    //tab - used to split the same gui into diffrent tabs. Not used for grouping diffrent guis together with tabs
//    @SideOnly(Side.CLIENT)
//    public abstract GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab);

    @OnlyIn(Dist.CLIENT)
    public abstract TabbedGuiContainer<MachineModuleContainer> createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab);
    public abstract MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab);
    public abstract ITextComponent createTitle(int tab);

    public void openContainer(ServerPlayerEntity player, int tab) {
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
            (id, inv, p) -> this.createContainer(id, p, tab),
            this.createTitle(tab))
        );
    }

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
            ProjectNublar.NETWORK.sendToServer(new C2SChangeContainerTab(this.tab, MachineModuleBlockEntity.this.worldPosition));
        }
    }
}
