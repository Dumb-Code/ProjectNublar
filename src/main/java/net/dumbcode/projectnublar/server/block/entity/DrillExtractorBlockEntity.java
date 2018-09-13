package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.DrillExtractorGui;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.recipes.DrillExtractorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class DrillExtractorBlockEntity extends MachineModuleBlockEntity<DrillExtractorBlockEntity> {
    @Override
    protected int getInventorySize() {
        return 5;
    }

    @Override
    public boolean isItemValidFor(int slot, ItemStack stack) {
        if(slot > 0) {
            return stack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
        }
        return true;
    }

    @Override
    protected List<MachineRecipe<DrillExtractorBlockEntity>> getAllRecipes() {
        return Lists.newArrayList(
                new DrillExtractorRecipe(new ResourceLocation("test"),
                        stack -> stack.getItem() == ItemHandler.AMBER,
                        stack -> new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(Dinosaur.getRandom())),
                        20)
        );
    }

    @Override
    protected DrillExtractorBlockEntity asB() {
        return this;
    }

    @Override
    protected List<MachineProcess<DrillExtractorBlockEntity>> createProcessList() {
        return Lists.newArrayList(
                new MachineProcess<>(new int[]{0}, new int[]{4, 5, 6, 7})
        );
    }

    @Override
    public GuiScreen createScreen(EntityPlayer player) {
        return new DrillExtractorGui(player, this);
    }

    @Override
    public Container createContainer(EntityPlayer player) {
        return new MachineModuleContainer(player, 100,
                new MachineModuleSlot(this, 0, 77, 12), //input
                new MachineModuleSlot(this, 1, 96, 62), //test tube 1
                new MachineModuleSlot(this, 2, 114, 62), //test tube 2
                new MachineModuleSlot(this, 3, 132, 62), //test tube 3
                new MachineModuleSlot(this, 4, 150, 62) //test tube 4
        );
    }
}
