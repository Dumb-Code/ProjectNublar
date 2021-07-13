package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.C2SPlaceIncubatorEgg;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class IncubatorScreen extends MachineContainerScreen {

    private final IncubatorBlockEntity blockEntity;

    private boolean justClickedEggPlacement;

    public IncubatorScreen(IncubatorBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
        this.height = 200;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        int left = this.leftPos + 38;
        int top = this.topPos + 8;
        int right = this.leftPos + this.width - 38;
        int bottom = this.topPos + 108;

        for (int i = 0; i < 9; i++) {
            Slot slotRaw = this.menu.getSlot(i + 1);
            if(slotRaw instanceof MachineModuleSlot) {
                MachineModuleSlot slot = (MachineModuleSlot) slotRaw;
                IncubatorBlockEntity.Egg egg = this.blockEntity.getEggList()[i];
                if (egg != null) {
                    this.menu.slots.set(i + 1, slot = new MachineModuleSlot(
                        this.blockEntity, slot.getSlotIndex(),
                        egg.getXPos() + left - this.leftPos - 8,
                        egg.getYPos() + top - this.topPos - 8
                    ));
                    slot.setEnabled(true);
                } else { //Should always be true
                    slot.setEnabled(false);
                }
            }
        }

        super.render(stack, mouseX, mouseY, partialTicks);

        ItemStack itemStack = Minecraft.getInstance().player.inventory.getCarried();
        int color = 0xFF000000;
        boolean holdingEgg = ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(itemStack.getItem());
        List<IncubatorBlockEntity.Egg> eggs = this.blockEntity.getCollidingEggs(mouseX - left, mouseY - top);
        if(mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom && holdingEgg) {
            color = eggs.isEmpty() ? 0xFF00FF00 : 0xFFFF0000;
        }
        fill(stack, left, top, right, bottom, color);
        if(holdingEgg) {
            for (IncubatorBlockEntity.Egg egg : eggs) {
                fill(stack,
                    Math.max(left + egg.getXPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, left),
                    Math.max(top + egg.getYPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, top),
                    Math.min(left + egg.getXPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, right),
                    Math.min(top + egg.getYPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, bottom),
                    -1
                );
            }
        }


        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int state) {
        int left = this.leftPos + 38;
        int top = this.topPos + 8;
        int right = this.leftPos + this.width - 38;
        int bottom = this.topPos + 108;

        ItemStack stack = minecraft.player.inventory.getCarried();
        if(mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(stack.getItem())) {
            ProjectNublar.NETWORK.sendToServer(new C2SPlaceIncubatorEgg((int) mouseX - left, (int) mouseY - top));
            this.justClickedEggPlacement = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        if(this.justClickedEggPlacement) {
            this.justClickedEggPlacement = false;
            return false;
        }
        return super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
    }


    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/incubator.png"));
        blit(stack, this.leftPos, this.topPos, 0, 0, this.width, this.height);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        this.minecraft.textureManager.bind(slotLocation);
        for(Slot slot : this.menu.slots) {
            blit(stack, this.leftPos + slot.x - 1, this.topPos + slot.y- 1, 7, 17, 18, 18);
        }
    }
}
