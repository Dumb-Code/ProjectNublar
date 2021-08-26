package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.dumblibrary.server.network.NetworkUtils;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.network.C2SChangeContainerTab;
import net.dumbcode.projectnublar.server.network.S2CSyncMachineProcesses;
import net.dumbcode.projectnublar.server.network.S2CSyncMachineStack;
import net.dumbcode.projectnublar.server.network.S2CSyncOpenedUsers;
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
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
    private MachineModuleEnergyStorage energy;

    private final LazyOptional<EnergyStorage> energyCapability = LazyOptional.of(() -> this.energy);

    @Getter @Setter
    private int clientMaxEnergy;
    @Getter @Setter
    private int clientEnergyHeld;

    public MachineModuleBlockEntity(TileEntityType<?> type) {
        super(type);
        this.energy = new MachineModuleEnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), 50);//getEnergyMaxExtractSpeed()
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

            ListNBT blockedList = new ListNBT();
            for (BlockedProcess blockedProcess : process.blockedProcessList) {
                CompoundNBT blockedNbt = new CompoundNBT();
                blockedNbt.putIntArray("Slots", blockedProcess.getSlots());
                blockedNbt.put("Item", blockedProcess.getStack().serializeNBT());
                blockedList.add(blockedNbt);
            }
            nbt.put("Blocked", blockedList);

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

            List<BlockedProcess> list = process.getBlockedProcessList();
            list.clear();
            for (INBT blocked : nbt.getList("Blocked", Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT blockedNbt = (CompoundNBT) blocked;
                list.add(new BlockedProcess(
                    ItemStack.of(blockedNbt.getCompound("Item")),
                    blockedNbt.getIntArray("Slots")
                ));
            }
        }

        CompoundNBT energyNBT = compound.getCompound("Energy");
        energy = new MachineModuleEnergyStorage(getEnergyCapacity(), getEnergyMaxTransferSpeed(), getEnergyMaxExtractSpeed(), energyNBT.getInt("Amount"));
    }

    @Override
    public void tick() {
        if(this.level != null && !this.level.isClientSide) {
            updateEnergyNetwork();
            int i = energy.extractRaw(getBaseEnergyConsumption());
            boolean hasPower = i >= getBaseEnergyConsumption();
            for (MachineProcess<B> process : this.processes) {
                if (!canProvideEnergyForProcess(process)) {
                    process.setHasPower(false);
                    this.getInterruptAction(process, ProcessInterruptReason.NO_POWER).processConsumer.accept(process);
                    continue;
                }
                process.setHasPower(true);
                if (hasPower && this.canProcess(process) && (process.currentRecipe == null || process.currentRecipe.accepts(this.asB, process))) {
                    if (process.isProcessing() || this.searchForRecipes(process, false)) {
                        if (process.isFinished() && !process.isBlocked()) {
                            MachineRecipe<B> recipe = process.getCurrentRecipe();
                            if (recipe != null) {
                                recipe.onRecipeFinished(this.asB, process);
                                process.setTime(0);
                                if (!recipe.startsAutomatically() || !recipe.accepts(this.asB, process)) {
                                    process.setProcessing(false);
                                    process.setTotalTime(0);
                                    process.setTime(0);
                                    process.setCurrentRecipe(null);
                                    this.searchForRecipes(process, false);
                                } else {
                                    recipe.onRecipeStarted(asB(), process);
                                }
                            } else {
                                ProjectNublar.getLogger().error("Unable to find recipe " + process.getCurrentRecipe() + " as it does not exist.");
                            }
                        } else {
                            energy.extractRaw(process.getCurrentConsumptionPerTick()); // consume energy for process
                            energy.receiveEnergy(process.getCurrentProductionPerTick(), false);
                            process.tick();
                        }
                        this.setChanged();
                    } else if(process.isBlocked()) {
                        process.tick();
                    }
                } else if (process.isProcessing()) {
                    this.getInterruptAction(process, ProcessInterruptReason.INVALID_INPUTS).processConsumer.accept(process);
                }
            }
            //todo: only sync when needed
            ProjectNublar.NETWORK.send(NetworkUtils.forPos(this.level, this.worldPosition),
                new S2CSyncMachineProcesses(
                    this.worldPosition,
                    this.processes.stream()
                        .map(S2CSyncMachineProcesses.ProcessSync::of)
                        .collect(Collectors.toList()),
                    this.energy.getEnergyStored(),
                    this.energy.getMaxEnergyStored(),
                    this.gatherExtraSyncData()
            ));
        } else {
//            for (MachineProcess<B> process : this.processes) {
//                if(process.isProcessing() && !process.isFinished() && process.isHasPower() && !process.isBlocked()) {
//                    process.setTime(process.getTime() + 1);
//                }
//            }
        }
    }

    protected int[] gatherExtraSyncData() {
        return new int[0];
    }

    public void onExtraSyncData(int[] aint) {
    }

    private boolean canProvideEnergyForProcess(MachineProcess<B> process) {
        int amountToProvide = process.getCurrentConsumptionPerTick();
        return energy.getEnergyStored() >= amountToProvide;
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
            int maxExtractable = neighbor.extractEnergy(Integer.MAX_VALUE, true);
            int maxReceivable = energy.receiveEnergy(maxExtractable, true);
           //  actual transfer
            energy.receiveEnergy(neighbor.extractEnergy(maxReceivable, false), false);
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

    public boolean searchForRecipes(int processId, boolean manualInput) {
        return this.searchForRecipes(this.getProcess(processId), manualInput);
    }

    public boolean searchForRecipes(MachineProcess<B> process, boolean manualInput) {
        if(!this.level.isClientSide) {
            for (MachineRecipe<B> recipe : this.recipes) {
                if(this.canProcess(process) && (manualInput || recipe.startsAutomatically()) && process.canAcceptRecipe(recipe) && recipe.accepts(this.asB, process)) {
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

    protected boolean canProcess(MachineProcess<B> process) {
        return true;
    }

    protected ProcessInterruptAction getInterruptAction(MachineProcess<B> process, ProcessInterruptReason reason) {
        if(process.currentRecipe != null) {
            return process.currentRecipe.getInterruptAction(this.asB, process, reason);
        }
        return ProcessInterruptAction.RESET;
    }

    protected void onSlotChanged(int slot) {
        if(!this.level.isClientSide) {
            ProjectNublar.NETWORK.send(NetworkUtils.forPos(this.level, this.worldPosition), new S2CSyncMachineStack(this, slot));
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
    public abstract MachineContainerScreen createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab);
    public abstract MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab);
    public abstract ITextComponent createTitle(int tab);

    public void openContainer(ServerPlayerEntity player, int tab) {
        ProjectNublar.NETWORK.send(NetworkUtils.forPos(this.level, this.worldPosition), new S2CSyncOpenedUsers(this.worldPosition, this.getOpenedUsers()));
        NetworkHooks.openGui(player, new SimpleNamedContainerProvider(
            (id, inv, p) -> this.createContainer(id, p, tab),
            this.createTitle(tab)),
            buffer -> {
                buffer.writeBlockPos(this.worldPosition);
                buffer.writeInt(tab);
            }
        );
    }

    @Setter
    @Getter
    public static class MachineProcess<B extends MachineModuleBlockEntity<B>> {
        private final int[] inputSlots;
        private final int[] outputSlots;

        private final int[] allSlots;
        private final MachineModuleBlockEntity<B> machine;

        private final List<BlockedProcess> blockedProcessList = new ArrayList<>();

        private final Predicate<MachineRecipe<B>> recipePredicate;

        private int time;
        private int totalTime;
        @Nullable
        MachineRecipe<B> currentRecipe;
        boolean processing;
        boolean hasPower;


        @SafeVarargs
        public MachineProcess(MachineModuleBlockEntity<B> machine, int[] inputSlots, int[] outputSlots, MachineRecipe<B>... recipesToAccept) {
            this(machine, inputSlots, outputSlots, Util.<Predicate<MachineRecipe<B>>>make(() -> {
                if(recipesToAccept.length == 0) {
                    return bMachineRecipe -> true;
                }
                HashSet<MachineRecipe<B>> recipes = Sets.newHashSet(recipesToAccept);
                return recipes::contains;
            }));
        }

        public MachineProcess(MachineModuleBlockEntity<B> machine, int[] inputSlots, int[] outputSlots, Predicate<MachineRecipe<B>> predicate) {
            this.inputSlots = inputSlots;
            this.outputSlots = outputSlots;
            this.machine = machine;
            this.allSlots = ArrayUtils.addAll(this.inputSlots, this.outputSlots);
            this.recipePredicate = predicate;
        }

        public int getInputSlot(int index) {
            return this.inputSlots[index];
        }

        public int getOutputSlot(int index) {
            return this.outputSlots[index];
        }

        public boolean insertOutputItem(ItemStack stack, int... slots) {
            if(slots.length == 0) {
                slots = IntStream.range(0, this.outputSlots.length).toArray();
            }
            for (int slot : slots) {
                if(stack.isEmpty()) {
                    return true;
                }
                stack = this.machine.getHandler().insertOutputItem(this.getOutputSlot(slot), stack, false);
            }
            if(!stack.isEmpty()) {
                this.blockOutputSlots(stack, slots);
                return false;
            }
            return true;
        }

        public void blockOutputSlots(ItemStack stack, int... slots) {
            if(slots.length == 0) {
                slots = IntStream.range(0, this.outputSlots.length).toArray();
            }
            if(!stack.isEmpty()) {
                this.blockedProcessList.add(new BlockedProcess(stack, slots));
            }
        }

        public int getCurrentConsumptionPerTick() {
            if(isFinished() || this.isBlocked())
                return 0;
            if(currentRecipe != null)
                return currentRecipe.getCurrentConsumptionPerTick(machine.asB, this);
            return 0;
        }

        public float getTimeDone() {
            return (float) this.time / this.totalTime;
        }

        public int getTimeDone(int modifier) {
            return (int) (this.getTimeDone() * modifier);
        }

        public int getCurrentProductionPerTick() {
            if(isFinished() || this.isBlocked())
                return 0;
            if(currentRecipe != null)
                return currentRecipe.getCurrentProductionPerTick(machine.asB, this);
            return 0;
        }

        public void tick() {
            if(this.isBlocked()) {
                for (Iterator<BlockedProcess> iterator = this.blockedProcessList.iterator(); iterator.hasNext(); ) {
                    BlockedProcess blockedProcess = iterator.next();
                    for (int slot : blockedProcess.getSlots()) {
                        int outputSlot = this.getOutputSlot(slot);
                        blockedProcess.setStack(this.machine.getHandler().insertOutputItem(outputSlot, blockedProcess.getStack(), false));
                        if(blockedProcess.getStack().isEmpty()) {
                            iterator.remove();
                        }
                    }

                }
            } else {
                this.time++;
                if(currentRecipe != null) {
                    this.totalTime = currentRecipe.getRecipeTime(machine.asB, this);
                    currentRecipe.onRecipeTick(machine.asB, this);
                }
            }
        }

        public boolean isFinished() {
            return this.time >= this.totalTime && !this.isBlocked();
        }

        public boolean isBlocked() {
            return !this.blockedProcessList.isEmpty();
        }

        public boolean canAcceptRecipe(MachineRecipe<B> recipe) {
            return this.recipePredicate.test(recipe);
        }


        public void causeSlotResetIfNecessary(int slot) {
            if(!this.processing || this.currentRecipe == null) {
                return;
            }
            int idx = -1;
            for (int i = 0; i < this.inputSlots.length; i++) {
                if(this.inputSlots[i] == slot) {
                    idx = i;
                }
            }
            if(idx != -1 && this.currentRecipe.shouldSlotChangeCauseReset(this.machine.asB, this, idx)) {
                this.time = 0;
            }
        }

        public void causeGlobalSlotResetIfNecessary(int slot) {
            if(this.processing && this.currentRecipe != null && slot != -1 && this.currentRecipe.shouldGlobalSlotChangeCauseReset(this.machine.asB, this, slot)) {
                this.time = 0;
            }
        }
    }

    @AllArgsConstructor
    @Data
    public static class BlockedProcess {
        private ItemStack stack;
        private final int[] slots;
    }

    public enum ProcessInterruptAction {
        RESET(p -> {
            p.setProcessing(false);
            p.setTotalTime(0);
            p.setTime(0);
            p.setCurrentRecipe(null);
        }),
        DECREASE(p -> p.setTime(p.getTime() - 1)),
        PAUSE(p -> {});

        private final Consumer<MachineProcess> processConsumer;

        ProcessInterruptAction(Consumer<MachineProcess> processConsumer) {
            this.processConsumer = processConsumer;
        }
    }

    public enum ProcessInterruptReason {
        NO_POWER, INVALID_INPUTS
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

    @SuppressWarnings("unchecked")
    public static Class<MachineModuleBlockEntity<?>> getWildcardType() {
        return (Class<MachineModuleBlockEntity<?>>) (Class<?>) MachineModuleBlockEntity.class;
    }
}
