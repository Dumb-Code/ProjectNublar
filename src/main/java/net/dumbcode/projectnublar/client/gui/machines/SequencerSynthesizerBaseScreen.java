package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModulePopoutSlot;
import net.dumbcode.projectnublar.server.containers.machines.slots.SlotCanBeDisabled;
import net.dumbcode.projectnublar.server.network.C2SChangeContainerTab;
import net.dumbcode.projectnublar.server.network.C2SMachineContainerPopoutSlotOpened;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SequencerSynthesizerBaseScreen extends MachineContainerScreen {

    private static final ResourceLocation BASE_LOCATION = get("base");
    private static final ResourceLocation CENTER_PIECES = get("center_pieces");
    private static final ResourceLocation INVENTORY_OVERLAY = get("inventory_overlay");

    private static final int RING_SIZE = 175;

    private static final int TAB_WIDTH = 79;
    private static final int TAB_HEIGHT = 10;

    private static final int TAB_Y_OFFSET = 4;
    private static final int[] TAB_X_OFFSET = { 92, 178, 264 };
    private static final TranslationTextComponent[] TAB_TITLES = {
            ProjectNublar.translate("gui.machine.sequencer.tab.sequencing"),
            ProjectNublar.translate("gui.machine.sequencer.tab.editing"),
            ProjectNublar.translate("gui.machine.sequencer.tab.synthesizing"),
    };

    private final float[] ringModifiers = new float[5];
    protected final List<SlotCanBeDisabled> slots;

    protected final BlockPos sequencerBlockPos;
    protected MachineModulePopoutSlot activeSlot;

    public SequencerSynthesizerBaseScreen(MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.imageWidth = 351;
        this.imageHeight = 199;
        this.sequencerBlockPos = inventorySlotsIn.getBlockEntity().getBlockPos();
        this.slots = inventorySlotsIn.disableSlots;

        Random random = new Random(this.sequencerBlockPos.asLong());
        for (int i = 0; i < this.ringModifiers.length; i++) {
            this.ringModifiers[i] = random.nextFloat() * 0.5F + 0.25F;
        }

        for (SlotCanBeDisabled slot : this.slots) {
            slot.setActive(false);
        }

    }

    @Override
    protected void renderLabels(MatrixStack stack, int mouseX, int mouseY) {
        for (int i = 0; i < 3; i++) {
            int width = this.font.width(TAB_TITLES[i].getVisualOrderText());
            this.font.draw(stack, TAB_TITLES[i], TAB_X_OFFSET[i] + (TAB_WIDTH - width) / 2F, TAB_Y_OFFSET, -1);
        }
        if(this.activeSlot != null) {
            stack.pushPose();
            stack.translate(0, 0, 400);
            this.font.draw(stack, this.activeSlot.getTextComponent(), 95, 37, 0xFF555555);
            stack.popPose();
        }
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        if(this.activeSlot != null) {
            return Collections.emptyList();
        }
        return super.children();
    }

    @Override
    protected void drawProcessTooltip(MachineModuleBlockEntity.MachineProcess<?> process, MatrixStack stack, int xStart, int yStart, int width, int height, int mouseX, int mouseY) {
        if(this.activeSlot != null) {
            return;
        }
        super.drawProcessTooltip(process, stack, xStart, yStart, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        for (int i = 0; i < 3; i++) {
            if(x >= TAB_X_OFFSET[i] && x < TAB_X_OFFSET[i]+TAB_WIDTH && y >= TAB_Y_OFFSET && y < TAB_Y_OFFSET+TAB_HEIGHT) {
                ProjectNublar.NETWORK.sendToServer(new C2SChangeContainerTab(i, this.sequencerBlockPos));
                return true;
            }
        }
        if(this.activeSlot == null) {
            List<Slot> slotList = this.menu.slots;
            for (int i = 0; i < slotList.size(); i++) {
                Slot slot = slotList.get(i);
                if (slot instanceof MachineModulePopoutSlot) {
                    MachineModulePopoutSlot popoutSlot = (MachineModulePopoutSlot) slot;
                    if (this.isHovering(popoutSlot.getVisualShowX(), popoutSlot.getVisualShowY(), 16, 16, mouseX, mouseY)) {
                        this.activeSlot = popoutSlot;
                        this.slots.get(i).setActive(true);
                        for (int s = this.slots.size() - 36; s < this.slots.size(); s++) {
                            this.slots.get(s).setActive(true);
                        }

                        int v = slot.getSlotIndex();
                        this.menu.setPredicate(value -> value == v);
                        ProjectNublar.NETWORK.sendToServer(new C2SMachineContainerPopoutSlotOpened(v));
                        return true;
                    }
                }
            }
        } else if(x < 87 || y < 31 || x > 262 || y > 166) {
            for (SlotCanBeDisabled slot : this.slots) {
                slot.setActive(false);
            }
            this.menu.setPredicate(value -> true);
            ProjectNublar.NETWORK.sendToServer(new C2SMachineContainerPopoutSlotOpened(-1));
            this.activeSlot = null;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public static void renderEntityAt(int x, int y, double scale, Entity entity) {
        renderEntityAt(x, y, scale, entity, 1000, Vector3f.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getFrameTime()));
    }

    public static void renderEntityAt(int x, int y, double scale, Entity entity, double zLevel, Quaternion rotation) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)x, (float)y, 1550.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, zLevel);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        quaternion.mul(rotation);
        matrixstack.mulPose(quaternion);
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        rotation.conj();
        entityrenderermanager.overrideCameraOrientation(rotation);
        entityrenderermanager.setRenderShadow(false);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();

        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() ->
            entityrenderermanager.render(entity, 0, 0, 0, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880)
        );

        irendertypebuffer$impl.endBatch();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }



    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        boolean background = this.shouldRenderBackground();
        if(background && this.activeSlot == null) {
            this.renderBackground(stack);
        }

        super.render(stack, mouseX, mouseY, ticks);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.leftPos, this.topPos, 0);
        for (Slot slot : this.menu.slots) {
            if(slot instanceof MachineModulePopoutSlot) {
                ItemStack itemstack = slot.getItem();
                int i = ((MachineModulePopoutSlot) slot).getVisualShowX();
                int j = ((MachineModulePopoutSlot) slot).getVisualShowY();
                RenderSystem.enableDepthTest();
                this.itemRenderer.blitOffset -= 100;
                this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, i, j);
                this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, i, j, null);
                this.itemRenderer.blitOffset += 100;

                if(this.activeSlot == null) {
                    this.renderHoveredSlot(stack, i, j, mouseX, mouseY, super.getSlotColor(slot.index));
                }
            }
        }
        RenderSystem.popMatrix();


        this.renderScreen(stack, mouseX, mouseY, ticks);
        RenderSystem.enableDepthTest();

        boolean flag = true;

        if(this.activeSlot != null) {
            this.setBlitOffset(200);
            if(background) {
                this.renderBackground(stack);
            }

            minecraft.textureManager.bind(INVENTORY_OVERLAY);
            blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, this.imageHeight, this.imageWidth);

            RenderSystem.pushMatrix();
            RenderSystem.translatef(this.leftPos, this.topPos, 0);
            for (Slot slot : this.menu.slots) {
                if(slot.isActive()) {
                    this.renderHoveredSlot(stack, slot.x, slot.y, mouseX, mouseY, super.getSlotColor(slot.index));
                }
            }
            RenderSystem.popMatrix();
            this.setBlitOffset(0);
        } else if (this.minecraft.player.inventory.getCarried().isEmpty()) {
            for (Slot slot : this.menu.slots) {
                if(slot instanceof MachineModulePopoutSlot && slot.hasItem() && this.isHovering(((MachineModulePopoutSlot) slot).getVisualShowX(), ((MachineModulePopoutSlot) slot).getVisualShowY(), 16, 16, mouseX, mouseY)) {
                    this.renderTooltip(stack, slot.getItem(), mouseX, mouseY);
                    flag = false;
                    break;
                }
            }
        }

        if(flag) {
            this.renderTooltip(stack, mouseX, mouseY);
        }
    }

    @Override
    public int getSlotColor(int index) {
        return 0;
    }

    protected boolean shouldRenderBackground() {
        return true;
    }

    private void renderHoveredSlot(MatrixStack stack, int x, int y, double mouseX, double mouseY, int slotColor) {
        if(this.isHovering(x, y, 16, 16, mouseX, mouseY)) {
            this.setBlitOffset(300);
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(stack, x, y, x + 16, y + 16, slotColor, slotColor);
            RenderSystem.colorMask(true, true, true, true);
            this.setBlitOffset(0);
        }
    }

    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float ticks) {

    }


    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, this.imageHeight*2, this.imageWidth); //There is a vanilla bug that mixes up width and height

        minecraft.textureManager.bind(CENTER_PIECES);
        int ringStartX = (this.imageWidth - RING_SIZE) / 2;
        int ringStartY = (this.imageHeight - RING_SIZE) / 2;

        for (int ring = 0; ring < 5; ring++) {
            int u = (ring % 3) * RING_SIZE;
            int v = (ring / 3) * RING_SIZE;

            stack.pushPose();
            stack.translate(this.leftPos, this.topPos, 0);
            stack.translate(this.imageWidth / 2F, this.imageHeight / 2F, 0);
            stack.mulPose(Vector3f.ZP.rotationDegrees((minecraft.player.tickCount + minecraft.getFrameTime()) * (ring % 2 == 0 ? 1 : -1) * this.ringModifiers[ring] + 0.5F));
            stack.translate(-this.imageWidth / 2F, -this.imageHeight / 2F, 0);
            blit(stack, ringStartX, ringStartY, this.getBlitOffset(), u, v, RING_SIZE, RING_SIZE, 350, 525);
            stack.popPose();
        }

        this.renderCenterPiece(stack);

        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, this.imageHeight, this.imageWidth, this.imageHeight, this.imageHeight*2, this.imageWidth); //There is a vanilla bug that mixes up width and height

    }

    protected void renderCenterPiece(MatrixStack stack) {
        blit(stack, this.leftPos + (this.imageWidth - 63) / 2, this.topPos + (this.imageHeight - 63) / 2, this.getBlitOffset(), RING_SIZE*2, RING_SIZE, 63, 63,  350, 525);
    }

    protected static ResourceLocation get(String name) {
        return new ResourceLocation(ProjectNublar.MODID, "textures/gui/sequencer/" + name + ".png");
    }
}
