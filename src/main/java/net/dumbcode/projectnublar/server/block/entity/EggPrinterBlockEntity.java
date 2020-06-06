package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.client.gui.machines.EggPrinterGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.recipes.EggPrinterRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Getter
@Setter
public class EggPrinterBlockEntity extends MachineModuleBlockEntity<EggPrinterBlockEntity> implements DyableBlockEntity {
    private EnumDyeColor dye = EnumDyeColor.WHITE;

    //The following is stuff for the animations
    private final float[] snapshot = new float[8];
    private final int[] movementTicksLeft = new int[3];
    private final boolean[] previousStates = new boolean[3];


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
                new MachineProcess<>(this, new int[]{0, 1}, new int[]{2, 3})
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return new EggPrinterGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(this, player, 84, 176,
                new MachineModuleSlot(this, 0, 29, 24),
                new MachineModuleSlot(this, 1, 29, 42),
                new MachineModuleSlot(this, 2, 129, 24),
                new MachineModuleSlot(this, 3, 129, 42)
        );
    }

    @Override
    public void update() {
        super.update();
        for (int i = 0; i < this.movementTicksLeft.length; i++) {
            if(this.movementTicksLeft[i] >= 0) {
                this.movementTicksLeft[i]--;
            }
        }
    }

    @Override
    protected ProcessInterruptAction getInterruptAction(MachineProcess process) {
        return ProcessInterruptAction.PAUSE;
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
