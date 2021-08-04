package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class AdvancedDnaEditingScreen extends DnaEditingScreen {
    public AdvancedDnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(blockEntity, inventorySlotsIn, playerInventory, title, bar, "basic", 1);
    }

    @Override
    protected int getEntityLeft() {
        return 222;
    }

    @Override
    protected int getEntityRight() {
        return 340;
    }

    @Override
    protected GuiScrollBox<GuiScrollboxEntry> createOverviewScrollBox() {
        return new GuiScrollBox<>(this.leftPos + 222, this.topPos + 134, 118, 14, 3, this::createOverviewScrollBoxList);
    }
}
