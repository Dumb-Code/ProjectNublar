package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.EggPrinterGui;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.EggPrinterRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EggPrinterBlockEntity extends MachineModuleBlockEntity<EggPrinterBlockEntity> {
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
                new MachineProcess<>(this, new int[]{0, 1}, new int[]{3, 4})
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabInformationBar info, int tab) {
        return new EggPrinterGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(player, 84, 176,
                new MachineModuleSlot(this, 0, 29, 24),
                new MachineModuleSlot(this, 1, 29, 42),
                new MachineModuleSlot(this, 2, 129, 24),
                new MachineModuleSlot(this, 3, 129, 42)
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
        return 50;
    }
}
