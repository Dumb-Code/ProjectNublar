package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.SequencingSynthesizerGui;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.ChipInfomation;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.dumbcode.projectnublar.server.recipes.SequencingSynthesizerRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class SequencingSynthesizerBlockEntity extends MachineModuleBlockEntity<SequencingSynthesizerBlockEntity> {
    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    protected List<MachineRecipe<SequencingSynthesizerBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
                new SequencingSynthesizerRecipe(new ResourceLocation(ProjectNublar.MODID, "genetic_material"), 30,
                        stack -> stack.getItem() instanceof BasicDinosaurItem && ItemHandler.TEST_TUBES_GENETIC_MATERIAL.values().contains(stack.getItem()),
                        ChipInfomation::combine),

                new SequencingSynthesizerRecipe(new ResourceLocation(ProjectNublar.MODID, "filled_syringe"), 30,
                        stack -> stack.getItem() == ItemHandler.FILLED_SYRINGE,
                        ChipInfomation::combine)
        );
    }

    @Override
    protected SequencingSynthesizerBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<SequencingSynthesizerBlockEntity>> createProcessList() {
        return Lists.newArrayList(new MachineProcess<>(new int[]{0, 1}, new int[]{2}));
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player) {
        return new SequencingSynthesizerGui(player, this);
    }

    @Override
    public Container createContainer(EntityPlayer player) {
        return new MachineModuleContainer(player, 100,
                new MachineModuleSlot(this, 0, 62, 30),
                new MachineModuleSlot(this, 1, 98, 30),
                new MachineModuleSlot(this, 2, 80, 50));
    }
}
