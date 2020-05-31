package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.C41PlaceIncubatorEgg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;

public class IncubatorGuiScreen extends TabbedGuiContainer {

    private final IncubatorBlockEntity blockEntity;

    private boolean justClickedEggPlacement;

    public IncubatorGuiScreen(EntityPlayer player, IncubatorBlockEntity blockEntity, TabInformationBar info, int tab) {
        super(blockEntity.createContainer(player, tab), info);
        this.blockEntity = blockEntity;
        this.ySize = 200;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int left = this.guiLeft + 38;
        int top = this.guiTop + 8;
        int right = this.guiLeft + this.xSize - 38;
        int bottom = this.guiTop + 108;

        for (int i = 0; i < 9; i++) {
            Slot slotRaw = this.inventorySlots.getSlot(i + 1);
            if(slotRaw instanceof MachineModuleSlot) {
                MachineModuleSlot slot = (MachineModuleSlot) slotRaw;
                IncubatorBlockEntity.Egg egg = this.blockEntity.getEggList()[i];
                if(egg != null) {
                    slot.xPos = egg.getXPos() + left-this.guiLeft - 8;
                    slot.yPos = egg.getYPos() + top-this.guiTop - 8;
                    slot.setEnabled(true);
                } else { //Should always be true
                    slot.setEnabled(false);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        ItemStack stack = Minecraft.getMinecraft().player.inventory.getItemStack();
        int color = 0xFF000000;
        boolean holdingEgg = ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(stack.getItem());
        List<IncubatorBlockEntity.Egg> eggs = this.blockEntity.getCollidingEggs(mouseX - left, mouseY - top);
        if(mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom && holdingEgg) {
            color = eggs.isEmpty() ? 0xFF00FF00 : 0xFFFF0000;
        }
        Gui.drawRect(left, top, right, bottom, color);
        if(holdingEgg) {
            for (IncubatorBlockEntity.Egg egg : eggs) {
                Gui.drawRect(
                    Math.max(left + egg.getXPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, left),
                    Math.max(top + egg.getYPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, top),
                    Math.min(left + egg.getXPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, right),
                    Math.min(top + egg.getYPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, bottom),
                    -1
                );
            }
        }

        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int state) throws IOException {
        int left = this.guiLeft + 38;
        int top = this.guiTop + 8;
        int right = this.guiLeft + this.xSize - 38;
        int bottom = this.guiTop + 108;

        ItemStack stack = Minecraft.getMinecraft().player.inventory.getItemStack();
        if(mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(stack.getItem())) {
            ProjectNublar.NETWORK.sendToServer(new C41PlaceIncubatorEgg(this.blockEntity.getPos(), mouseX - left, mouseY - top));
            this.justClickedEggPlacement = true;
        }
        super.mouseClicked(mouseX, mouseY, state);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.justClickedEggPlacement) {
            this.justClickedEggPlacement = false;
        } else {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/incubator.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
