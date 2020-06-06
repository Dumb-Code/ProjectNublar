package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.DrillExtractorGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.DrillExtractorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class DrillExtractorBlockEntity extends MachineModuleBlockEntity<DrillExtractorBlockEntity> {
    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    protected List<MachineRecipe<DrillExtractorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
                DrillExtractorRecipe.INSTANCE
        );
    }

    @Override
    protected DrillExtractorBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<DrillExtractorBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(this, new int[]{0}, new int[]{1, 2, 3, 4})
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return new DrillExtractorGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(this, player, 84, 176,
                new MachineModuleSlot(this, 0, 77, 12), //input
                new MachineModuleSlot(this, 1, 50, 62), //test tube 1
                new MachineModuleSlot(this, 2, 68, 62), //test tube 2
                new MachineModuleSlot(this, 3, 86, 62), //test tube 3
                new MachineModuleSlot(this, 4, 104, 62) //test tube 4
        );
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
