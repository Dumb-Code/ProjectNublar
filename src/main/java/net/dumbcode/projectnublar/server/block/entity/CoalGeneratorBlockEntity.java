package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.client.gui.machines.CoalGeneratorScreen;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.recipes.CoalGeneratorRecipe;
import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

public class CoalGeneratorBlockEntity extends MachineModuleBlockEntity<CoalGeneratorBlockEntity> {

    public CoalGeneratorBlockEntity() {
        super(ProjectNublarBlockEntities.COAL_GENERATOR.get());
    }

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
        return Collections.singletonList(
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
    @OnlyIn(Dist.CLIENT)
    public MachineContainerScreen createScreen(MachineModuleContainer container, PlayerInventory inventory, ITextComponent title, TabInformationBar info, int tab) {
        return new CoalGeneratorScreen(container, inventory, title, info);
    }


    @Override
    public MachineModuleContainer createContainer(int windowId, PlayerEntity player, int tab) {
        return new MachineModuleContainer(windowId, this, player.inventory, tab, 84, 176,
            new MachineModuleSlot(this, 0, 78, 33)
        );
    }

    @Override
    public ITextComponent createTitle(int tab) {
        return new TranslationTextComponent(ProjectNublar.MODID + ".containers.coalgenerator.title");
    }
}
