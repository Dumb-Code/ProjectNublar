package net.dumbcode.projectnublar.client.gui.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModulePopoutSlot;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class MachineContainerScreen extends TabbedGuiContainer<MachineModuleContainer> {

    private static final ResourceLocation MACHINE_ICONS = new ResourceLocation(ProjectNublar.MODID, "textures/gui/machine_icons.png");

    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineContainerScreen(MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = inventorySlotsIn.getBlockEntity();
        inventorySlotsIn.addListener(this::onSlotChanged);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        fill(stack, this.leftPos - 8, this.topPos, this.leftPos, this.topPos + this.imageHeight, 0xFF8c8c8c);
        int heightOffset = (int) ((this.imageHeight - 6) * (1 - (float) this.blockEntity.getClientEnergyHeld() / this.blockEntity.getClientMaxEnergy()));
        fill(stack, this.leftPos - 5, this.topPos + 3, this.leftPos - 3, this.topPos + this.imageHeight - 3, 0xFFFF0000);
        fill(stack, this.leftPos - 5, this.topPos + 3 + heightOffset, this.leftPos - 3, this.topPos + this.imageHeight - 3, 0xFF00FF00);
        super.render(stack, mouseX, mouseY, ticks);
    }

    protected void bottomUpSubPixelBlit(MatrixStack stack, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight, float percent) {
        float inverseHeight = (1-percent)*height;
        subPixelBlit(stack, x, y + inverseHeight, u, v + inverseHeight, width, percent*height, textureWidth, textureHeight);
    }

    protected void subPixelBlit(MatrixStack stack, float x, float y, float u, float v, float width, float height) {
        this.subPixelBlit(stack, x, y, u, v, width, height, 256, 256);
    }

    protected void subPixelBlit(MatrixStack stack, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        Matrix4f matrix4f = stack.last().pose();
        int z = this.getBlitOffset();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x, y+height, z).uv(u / textureWidth, (v+height) / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x+width, y+height, z).uv((u+width) / textureWidth, (v+height) / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x+width, y, z).uv((u+width) / textureWidth, v / textureHeight).endVertex();
        bufferbuilder.vertex(matrix4f, x, y, z).uv(u / textureWidth, v / textureHeight).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(bufferbuilder);
    }

    protected void drawProcessIcon(MachineModuleBlockEntity.MachineProcess<?> process, MatrixStack stack, float xStartF, float yStartF) {
        int xStart = (int) Math.floor(xStartF);
        int yStart = (int) Math.floor(yStartF);
        stack.pushPose();
        stack.translate(xStartF - xStart, yStartF - yStart, 0);
        if(!process.isHasPower()) {
            minecraft.textureManager.bind(MACHINE_ICONS);
            blit(stack, this.leftPos + xStart, this.topPos + yStart, this.getBlitOffset(), 16, 0, 16, 16, 32, 32);
            stack.popPose();
        } else if(process.isBlocked()) {
            minecraft.textureManager.bind(MACHINE_ICONS);
            blit(stack, this.leftPos + xStart, this.topPos + yStart, this.getBlitOffset(), 0, 0, 16, 16, 32, 32);
            stack.popPose();

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

                                fill(stack, this.leftPos + x, this.topPos + y, this.leftPos + x + 16, this.topPos + y + 16, 0xA0FF0000);

                            }
                        }
                    }
                }
            }
            RenderSystem.enableDepthTest();
        }
    }

    protected void drawProcessTooltip(MachineModuleBlockEntity.MachineProcess<?> process, MatrixStack stack, int xStart, int yStart, int width, int height, int mouseX, int mouseY) {
        int mX = mouseX - this.leftPos;
        int mY = mouseY - this.topPos;

        if(mX >= xStart && mX < xStart+width && mY >= yStart && mY < yStart+height) {
            List<ITextComponent> componentList = new ArrayList<>();


            componentList.add(process.getTimeLeftText().append("(" + Math.round(process.getTimeDone() * 100F) + "%)"));
            if(!process.isHasPower()) {
                componentList.add(ProjectNublar.translate("gui.machine.nopower"));
            } else if(process.isBlocked()) {
                componentList.add(ProjectNublar.translate("gui.machine.fullslot"));
            }
            renderComponentTooltip(stack, componentList, mouseX, mouseY);
        }
    }

    public void onSlotChanged(int slot, ItemStack stack) {

    }
}
