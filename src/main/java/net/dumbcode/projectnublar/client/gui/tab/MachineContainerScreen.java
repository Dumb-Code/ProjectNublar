package net.dumbcode.projectnublar.client.gui.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModulePopoutSlot;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class MachineContainerScreen extends TabbedGuiContainer<MachineModuleContainer> {
    private static final ResourceLocation MACHINE_ICONS = new ResourceLocation(ProjectNublar.MODID, "textures/gui/machine_icons.png");

    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineContainerScreen(MachineModuleContainer inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = inventorySlotsIn.getBlockEntity();
        inventorySlotsIn.addListener(this::onSlotChanged);
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float ticks) {
        stack.fill(this.leftPos - 8, this.topPos, this.leftPos, this.topPos + this.imageHeight, 0xFF8c8c8c);
        int heightOffset = (int) ((this.imageHeight - 6) * (1 - (float) this.blockEntity.getClientEnergyHeld() / this.blockEntity.getClientMaxEnergy()));
        stack.fill(this.leftPos - 5, this.topPos + 3, this.leftPos - 3, this.topPos + this.imageHeight - 3, 0xFFFF0000);
        stack.fill(this.leftPos - 5, this.topPos + 3 + heightOffset, this.leftPos - 3, this.topPos + this.imageHeight - 3, 0xFF00FF00);
        super.render(stack, mouseX, mouseY, ticks);
    }

    protected void bottomUpSubPixelBlit(GuiGraphics stack, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight, float percent) {
        float inverseHeight = (1-percent)*height;
        subPixelBlit(stack,  x, y + inverseHeight, u, v + inverseHeight, width, percent*height, textureWidth, textureHeight);
    }

    protected void subPixelBlit(GuiGraphics stack, float x, float y, float u, float v, float width, float height) {
        this.subPixelBlit(stack,  x, y, u, v, width, height, 256, 256);
    }

    protected void subPixelBlit(GuiGraphics stack, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        Matrix4f matrix4f = stack.pose().last().pose();
        int z = (int) stack.pose().last().pose().getTranslation(new Vector3f()).z;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x, y+height, z).uv(u / textureWidth, (v+height) / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x+width, y+height, z).uv((u+width) / textureWidth, (v+height) / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x+width, y, z).uv((u+width) / textureWidth, v / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, z).uv(u / textureWidth, v / textureHeight).endVertex();
        bufferbuilder.end();
        bufferbuilder.end();
    }

    protected void drawProcessIcon(MachineModuleBlockEntity.MachineProcess<?> process, GuiGraphics stack, float xStartF, float yStartF) {
        int xStart = (int) Math.floor(xStartF);
        int yStart = (int) Math.floor(yStartF);
        stack.pose().pushPose();
        stack.pose().translate(xStartF - xStart, yStartF - yStart, 0);
        if(!process.isHasPower()) {
            stack.blit(MACHINE_ICONS, this.leftPos + xStart, this.topPos + yStart, (int) stack.pose().last().pose().getTranslation(new Vector3f()).z, 16, 0, 16, 16, 32, 32);
            stack.pose().popPose();
        } else if(process.isBlocked()) {
            stack.blit(MACHINE_ICONS, this.leftPos + xStart, this.topPos + yStart, (int) stack.pose().last().pose().getTranslation(new Vector3f()).z, 0, 0, 16, 16, 32, 32);
            stack.pose().popPose();

            RenderSystem.disableDepthTest();
            for (MachineModuleBlockEntity.BlockedProcess blockedProcess : process.getBlockedProcessList()) {
                for (int iSlot : blockedProcess.getSlots()) {
                    int index = process.getOutputSlot(iSlot);
                    for (Slot slot : this.menu.slots) {
                        if(slot instanceof MachineModuleSlot) {
                            if(slot.getSlotIndex() == index) {
                                int x = slot.x;
                                int y = slot.y;
                                if(slot instanceof MachineModulePopoutSlot) {
                                    x = ((MachineModulePopoutSlot) slot).getVisualShowX();
                                    y = ((MachineModulePopoutSlot) slot).getVisualShowY();
                                }

                                stack.fill(this.leftPos + x, this.topPos + y, this.leftPos + x + 16, this.topPos + y + 16, 0xA0FF0000);

                            }
                        }
                    }
                }
            }
            RenderSystem.enableDepthTest();
        }
    }

    protected void drawProcessTooltip(MachineModuleBlockEntity.MachineProcess<?> process, GuiGraphics stack, int xStart, int yStart, int width, int height, int mouseX, int mouseY) {
        int mX = mouseX - this.leftPos;
        int mY = mouseY - this.topPos;

        if(mX >= xStart && mX < xStart+width && mY >= yStart && mY < yStart+height) {
            List<Component> componentList = new ArrayList<>();


            componentList.add(process.getTimeLeftText().append("(" + Math.round(process.getTimeDone() * 100F) + "%)"));
            if(!process.isHasPower()) {
                componentList.add(ProjectNublar.translate("gui.machine.nopower"));
            } else if(process.isBlocked()) {
                componentList.add(ProjectNublar.translate("gui.machine.fullslot"));
            }
            stack.renderComponentTooltip(this.font, componentList, mouseX, mouseY);
        }
    }

    public void onSlotChanged(int slot, ItemStack stack) {

    }
}
