package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.*;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.dna.data.ColouredGeneticDataHandler;
import net.dumbcode.dumblibrary.server.dna.data.GeneticTint;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticTypeOverallTintStorage;
import net.dumbcode.dumblibrary.server.utils.GeneticUtils;
import net.dumbcode.projectnublar.client.gui.machines.DnaEditingScreen;
import net.dumbcode.projectnublar.client.gui.machines.SequencingScreen;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerInputsScreen;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModulePopoutSlot;
import net.dumbcode.projectnublar.server.item.DriveItem;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerHardDriveRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.NBTTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

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
    public static final int MINIMUM_SLOTS = 3;
    public static final float SLOTS_START = 0.5F;
    public static final float SLOTS_END = 0.95F;
    //https://www.desmos.com/calculator/or6tpvq2mw
    private static final float SLOTS_GRADIENT = (TOTAL_SLOTS - MINIMUM_SLOTS) / (SLOTS_END - SLOTS_START);
    private static final float SLOTS_OFFSET = MINIMUM_SLOTS - SLOTS_GRADIENT*SLOTS_START;



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

    public double waterPercent() {
        return this.tank.getFluidAmount() / (double) this.tank.getCapacity();
    }

    public double sugarPercent() {
        return this.sugarAmount / this.totalStorage;
    }

    public double bonePercent() {
        return this.boneAmount / this.totalStorage;
    }

    public double plantPercent() {
        return this.plantAmount / this.totalStorage;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.tank.readFromNBT(compound.getCompound("FluidTank"));
        this.dye = DyeColor.byId(compound.getInt("Dye"));
        this.consumeTimer = compound.getInt("ConsumeTimer");

        ListNBT list = compound.getList("SelectedDnaList", 10);
        for (int i = 0; i < this.selectedDNAs.length; i++) {
            CompoundNBT tag = list.getCompound(i);
            this.selectedDNAs[i].setKey(tag.getString("Key"));
            this.selectedDNAs[i].setAmount(tag.getDouble("Amount"));
            this.selectedDNAs[i].setColorStorage(new DnaColourStorage(
                Arrays.stream(tag.getIntArray("Primary")).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
                Arrays.stream(tag.getIntArray("Secondary")).mapToObj(Integer::valueOf).collect(Collectors.toSet())
            ));

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
            tag.putIntArray("Primary", selectedDNA.getColorStorage().getPrimary().stream().mapToInt(value -> value).toArray());
            tag.putIntArray("Secondary", selectedDNA.getColorStorage().getSecondary().stream().mapToInt(value -> value).toArray());
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
                SequencingSynthesizerHardDriveRecipe.INSTANCE,
                SequencingSynthesizerRecipe.INSTANCE
        );
    }

    @Override
    protected SequencingSynthesizerBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<SequencingSynthesizerBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(this, new int[]{5}, new int[]{6}, SequencingSynthesizerHardDriveRecipe.INSTANCE),
                new MachineProcess<>(this, new int[]{7}, new int[]{8}, SequencingSynthesizerRecipe.INSTANCE)
        );
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        switch (slot) {
            case 0: return stack.getItem() instanceof DriveItem;
            case 1: return MachineUtils.getWaterAmount(stack) != -1;
            case 2: return MachineUtils.getSugarMatter(stack) > 0;
            case 3: return MachineUtils.getBoneMatter(stack) > 0;
            case 4: return MachineUtils.getPlantMatter(stack, this.level, this.worldPosition) > 0;
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
    public void tick() {
        super.tick();

        Set<String> collect = DriveUtils.getAll(this.handler.getStackInSlot(0)).stream().map(d -> combine(d.getKey(), d.getVariant())).collect(Collectors.toSet());
        for (SelectedDnaEntry dna : this.selectedDNAs) {
            if((!dna.getKey().isEmpty() && !collect.contains(dna.getKey()))) {
                dna.clear();
            }
        }

        this.sugarAmount = this.updateAmount(this.sugarAmount, this.handler.getStackInSlot(2), MachineUtils::getSugarMatter);
        this.boneAmount = this.updateAmount(this.boneAmount, this.handler.getStackInSlot(3), MachineUtils::getBoneMatter);
        this.plantAmount = this.updateAmount(this.plantAmount, this.handler.getStackInSlot(4), stack -> MachineUtils.getPlantMatter(stack, this.level, this.worldPosition));
    }

    @Override
    public int[] constantInputSlots() {
        return new int[] { 0, 1, 2, 3, 4 };
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

    @Nonnull
    public FluidTank getTank() {
        return this.tank;
    }

    public boolean setAndValidateSelect(int ID, String key, double amount) {
        amount = MathHelper.clamp(amount, 0, 1);
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            //Check that the drive actually exists.
            Optional<DriveUtils.DriveEntry> drive = DriveUtils.getAll(this.handler.getStackInSlot(0)).stream().filter(d -> combine(d.getKey(), d.getVariant()).equals(key)).findAny();
            if(!drive.isPresent() && !key.isEmpty()) {
                return false;
            }
            //ID = 0 must be a dinosaur
            if(ID == 0 && !key.isEmpty() && drive.get().getDriveType() != DriveUtils.DriveType.DINOSAUR) {
                return false;
            }
            int amountInDrive = drive.map(DriveUtils.DriveEntry::getAmount).orElse(0);
            int slots = this.getSlots();
            double valueLeft = 1D;
            for (int i = 0; i < ID; i++) {
                if(key.equals(this.selectedDNAs[i].key) && !key.equals("")) {
                    return false;
                }
                if(i >= slots) {
                    this.selectedDNAs[i].clear();
                    continue;
                }
                double amountToSubtract = this.selectedDNAs[i].amount;
                if(i == 0 && amountToSubtract < 0.5) {
                    amountToSubtract = 0.5;
                }
                valueLeft -= amountToSubtract;

            }

            if(ID >= slots) {
                this.selectedDNAs[ID].clear();
                if(!this.level.isClientSide) {
                    this.getProcess(1).setTime(0);
                }
                return true;
            }
            //Make sure not less than whats left
            amount = Math.min(amount, valueLeft);
            //Make sure not less than we actually have
            amount = Math.min(amount, amountInDrive / 100D);
            valueLeft -= amount;
            this.setSelect(ID, key, amount);

            for (int i = ID + 1; i < TOTAL_SLOTS; i++) {
                if(this.selectedDNAs[i].key.equals(key) || i >= slots) {
                    this.selectedDNAs[i].clear();
                    continue;
                }
                if(valueLeft < this.selectedDNAs[i].amount) {
                    this.selectedDNAs[i].setAmount(valueLeft);
                }
                valueLeft -= this.selectedDNAs[i].amount;
            }
            if(!this.level.isClientSide) {
                this.getProcess(1).setTime(0);
            }
            return true;
        }
        return false;
    }

    public static String combine(String key, @Nullable String variant) {
        if(key.isEmpty()) {
            return "";
        }
        if(variant == null || variant.isEmpty()) {
            return key;
        }
        return key + "#" + variant;
    }

    public void setSelect(int ID, String key, double amount) {
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            this.selectedDNAs[ID].setKey(key);
            this.selectedDNAs[ID].setAmount(amount);
        }
    }

    public void setStorage(int ID, DnaColourStorage storage) {
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            this.selectedDNAs[ID].setColorStorage(storage);
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

    public DnaColourStorage getStorage(int ID) {
        if(ID >= 0 && ID < this.selectedDNAs.length) {
            return this.selectedDNAs[ID].getColorStorage();
        }
        return null;
    }

     @Override
    protected void addTabs(List<TabInformationBar.Tab> tabList) {
        tabList.add(new DefaultTab(0));
//        tabList.add(new DefaultTab(1));
//        tabList.add(new DefaultTab(2));
    }

    @Override
    public MachineContainerScreen createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        switch (tab) {
            case 0:
                return new SequencingScreen(this, container, inventory, title, info);
            case 1:
                return new DnaEditingScreen(this, container, inventory, title, info);
            default:
                return new SequencingSynthesizerInputsScreen(this, container, title, info, inventory);
        }
    }

    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        switch (tab) {
            case 0:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, 85, 351,
                    new MachineModulePopoutSlot(this, 0, 167, 61, 27, 32, createMatterTranslation("insert", "hard_drive")),
                    new MachineModulePopoutSlot(this, 5, 167, 61, 27, 153, createMatterTranslation("insert", "genetic_material")),
                    new MachineModulePopoutSlot(this, 6, 167, 61, 308, 153, createMatterTranslation("remove", "leftover"))
                );
            case 1:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, -1, 208);
            default:
                return new MachineModuleContainer(windowId, this, player.inventory, tab, 85, 351,
                    new MachineModulePopoutSlot(this, 1, 167, 61,49, 37, createMatterTranslation("insert", "water")), //water
                    new MachineModulePopoutSlot(this, 2, 167, 61,49, 107, createMatterTranslation("insert", "sugar")), //sugar
                    new MachineModulePopoutSlot(this, 3, 167, 61,285, 37, createMatterTranslation("insert", "bone")), //bone
                    new MachineModulePopoutSlot(this, 4, 167, 61,285, 107, createMatterTranslation("insert", "plant")), //plant
                    new MachineModulePopoutSlot(this, 7, 167, 61, 129, 169, createMatterTranslation("insert", "test_tube")),
                    new MachineModulePopoutSlot(this, 8, 167, 61, 205, 169, createMatterTranslation("remove", "dna_test_tube"))
                );
        }
    }

    private static ITextComponent createMatterTranslation(String operation, String name) {
        return ProjectNublar.translate("gui.machine.sequencer." + operation, ProjectNublar.translate("gui.machine.sequencer.matter." + name));
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
        private DnaColourStorage colorStorage = new DnaColourStorage(new HashSet<>(), new HashSet<>());

        public void clear() {
            this.key = "";
            this.amount = 0;
            this.colorStorage = new DnaColourStorage(new HashSet<>(), new HashSet<>());
        }
    }

    @Data
    @AllArgsConstructor
    public static class DnaColourStorage {
        private Set<Integer> primary;
        private Set<Integer> secondary;
    }

    public int getDinosaurAmount() {
        if(this.selectedDNAs[0].key.isEmpty()) {
            return 0;
        }
        return DriveUtils.getAmount(this.getHandler().getStackInSlot(0), this.selectedDNAs[0].key, null);
    }

    public int getSlots() {
        return getSlots(this.getDinosaurAmount() / 100F);
    }

    public List<GeneticEntry<?, ?>> gatherAllGeneticEntries() {
        List<GeneticEntry<?, ?>> out = new ArrayList<>();
        ItemStack drive = this.handler.getStackInSlot(0);
        int slots = this.getSlots();
        Map<String, DriveUtils.DriveEntry> collected = DriveUtils.getAll(drive).stream().collect(Collectors.toMap(d -> combine(d.getKey(), d.getVariant()), entry -> entry));
        for (int i = 0; i < slots; i++) {
            if(this.selectedDNAs[i].key.isEmpty() || this.selectedDNAs[i].amount == 0) {
                continue;
            }
            DriveUtils.DriveEntry entry = collected.get(this.selectedDNAs[i].key);
            if(entry == null) {
                ProjectNublar.getLogger().warn("Illegal Drive Entry. Tried to get {} in list of {}", this.selectedDNAs[i].key, collected.keySet());
                continue;
            }
            if(entry.getDriveType() == DriveUtils.DriveType.OTHER) {
                ResourceLocation location = new ResourceLocation(entry.getKey());
                if(ForgeRegistries.ENTITIES.containsKey(location)) {
                    EntityType<?> value = ForgeRegistries.ENTITIES.getValue(location);
                    for (EntityGeneticRegistry.Entry<?, ?> e : EntityGeneticRegistry.INSTANCE.gatherEntry(value, entry.getVariant())) {
                        out.add(e.create((float) this.selectedDNAs[i].amount));
                    }

                    List<Integer> tints = EntityGeneticRegistry.INSTANCE.gatherTints(value, entry.getVariant());
                    List<GeneticTint.Part> allPrimaryColours = new ArrayList<>();
                    List<GeneticTint.Part> allSecondaryColours = new ArrayList<>();

                    DnaColourStorage storage = this.selectedDNAs[i].getColorStorage();
                    for (int t = 0; t < tints.size(); t++) {
                        int tint = tints.get(t);

                        if(storage.getPrimary().contains(t)) {
                            allPrimaryColours.add(new GeneticTint.Part(
                                ((tint >> 16) & 0xFF) / 255F,
                                ((tint >> 8) & 0xFF) / 255F,
                                ((tint) & 0xFF) / 255F,
                                1F,
                                (int) (GeneticUtils.DEFAULT_COLOUR_IMPORTANCE * this.selectedDNAs[i].amount)
                            ));
                        }
                        if(storage.getSecondary().contains(t)) {
                            allSecondaryColours.add(new GeneticTint.Part(
                                ((tint >> 16) & 0xFF) / 255F,
                                ((tint >> 8) & 0xFF) / 255F,
                                ((tint) & 0xFF) / 255F,
                                1F,
                                (int) (GeneticUtils.DEFAULT_COLOUR_IMPORTANCE * this.selectedDNAs[i].amount)
                            ));
                        }
                    }

                    GeneticTint.Part primary = ColouredGeneticDataHandler.combineMultipleParts(allPrimaryColours);
                    GeneticTint.Part secondary = ColouredGeneticDataHandler.combineMultipleParts(allSecondaryColours);

                    out.add(new GeneticEntry<>(GeneticTypes.OVERALL_TINT.get(), new GeneticTypeOverallTintStorage())
                        .setModifier(new GeneticTint(primary, secondary))
                    );
                }
            }
        }
        return out;
    }

    public static int getSlots(float percentage) {
        return MathHelper.clamp(MathHelper.floor(percentage*SLOTS_GRADIENT + SLOTS_OFFSET), MINIMUM_SLOTS, TOTAL_SLOTS);
    }

    public static long getPercentageForSlot(int slots) {
        if(slots <= MINIMUM_SLOTS) {
            return Math.round(SLOTS_START * 100D);
        }
        if(slots >= TOTAL_SLOTS) {
            return Math.round(SLOTS_END * 100D);
        }
        return Math.round((slots - SLOTS_OFFSET) / SLOTS_GRADIENT * 100D);
    }

    @Value
    public static class EntityGeneTintEntry {
        int tint;
        int index;
        DnaColourStorage colourStorage;
    }

}
