package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.CoalGeneratorGui;
import net.dumbcode.projectnublar.client.gui.tab.TabListInformation;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.CoalGeneratorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class CoalGeneratorBlockEntity extends MachineModuleBlockEntity<CoalGeneratorBlockEntity> {
    @Override
    public int getBaseEnergyProduction() {
        return 0;
    }

    @Override
    public int getBaseEnergyConsumption() {
        return 0;
    }

    @Override
    public int getEnergyCapacity() {
        return 50000;
    }

    @Override
    public int getEnergyMaxTransferSpeed() {
        return 100;
    }

    @Override
    public int getEnergyMaxExtractSpeed() {
        return 200;
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected List<MachineRecipe<CoalGeneratorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
            CoalGeneratorRecipe.INSTANCE
        );
    }

    @Override
    protected CoalGeneratorBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<CoalGeneratorBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(this, new int[]{0}, new int[0])
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, TabListInformation info, int tab) {
        return new CoalGeneratorGui(player, this, info, tab);
    }

    @Override
    public Container createContainer(EntityPlayer player, int tab) {
        return new MachineModuleContainer(player, 84, 176,
                new MachineModuleSlot(this, 0, 78, 33)
        );
    }
}
