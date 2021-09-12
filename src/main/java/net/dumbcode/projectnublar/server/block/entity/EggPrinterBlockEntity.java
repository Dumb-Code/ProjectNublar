package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.EggPrinterScreen;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.EggPrinterRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@Getter
@Setter
public class EggPrinterBlockEntity extends MachineModuleBlockEntity<EggPrinterBlockEntity> implements DyableBlockEntity {
    private DyeColor dye = DyeColor.WHITE;

    //The following is stuff for the animations
    private final float[] snapshot = new float[8];
    private final int[] movementTicksLeft = new int[3];
    private final boolean[] previousStates = new boolean[3];

    public static final int TOTAL_BONE_MATTER = 10;
    public static final int MINIMUM_BONE_MATTER = 5;
    private float boneMatter = 0;

    public EggPrinterBlockEntity() {
        super(ProjectNublarBlockEntities.EGG_PRINTER.get());
    }

    @Override
    protected int getInventorySize() {
        return 4;
    }

    @Override
    protected List<MachineRecipe<EggPrinterBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(EggPrinterRecipe.INSTANCE);
    }

    @Override
    protected EggPrinterBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<EggPrinterBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(this, new int[]{1}, new int[]{2, 3})
        );
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        if(slot == 0) {
            return MachineUtils.getBoneMatter(stack) > 0;
        }
        return super.isItemValidFor(slot, stack);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putFloat("boneMatter", boneMatter);
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        this.boneMatter = Math.min(compound.getFloat("boneMatter"), TOTAL_BONE_MATTER);
        super.load(state, compound);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public MachineContainerScreen createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        return new EggPrinterScreen(container, this, inventory, title, info);
    }

    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        return new MachineModuleContainer(windowId, this, player.inventory, tab, 148, 176,
            new MachineModuleSlot(this, 0, 15, 80), //Bone Meal
            new MachineModuleSlot(this, 1, 15, 110), //Embryo
            new MachineModuleSlot(this, 2, 148, 60), //Finished Egg
            new MachineModuleSlot(this, 3, 148, 110) //Empty Syringe
        );
    }

    @Override
    public ITextComponent createTitle(int tab) {
        return new TranslationTextComponent(ProjectNublar.MODID + ".containers.egg_printer.title");
    }

    @Override
    protected int[] gatherExtraSyncData() {
        return new int[] { Float.floatToRawIntBits(this.boneMatter) };
    }

    @Override
    public void onExtraSyncData(int[] aint) {
        this.boneMatter = Math.min(Float.intBitsToFloat(aint[0]), TOTAL_BONE_MATTER);
    }

    @Override
    public void tick() {
        if(!this.level.isClientSide && this.boneMatter < TOTAL_BONE_MATTER) {
            float toAdd = (float) MachineUtils.getBoneMatter(this.handler.getStackInSlot(0));
            if(toAdd > 0) {
                this.handler.getStackInSlot(0).shrink(1);
                this.boneMatter = Math.min(this.boneMatter + toAdd, TOTAL_BONE_MATTER);
            }
        }
        super.tick();
        for (int i = 0; i < this.movementTicksLeft.length; i++) {
            if(this.movementTicksLeft[i] >= 0) {
                this.movementTicksLeft[i]--;
            }
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
}
