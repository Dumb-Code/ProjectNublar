package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.network.C2SPlaceIncubatorEgg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IncubatorScreen extends MachineContainerScreen {

    private static final int TEXTURE_WIDTH = 334;
    private static final int TEXTURE_HEIGHT = 222;

    private static final int OVERLAY_START_X = 9;
    private static final int OVERLAY_START_Y = 9;


    private final IncubatorBlockEntity blockEntity;
    private final List<MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity>> processes;

    private static ShaderInstance shaderManager;


    private boolean justClickedEggPlacement;

    public IncubatorScreen(IncubatorBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
        this.processes = IntStream.range(0, 9)
            .mapToObj(blockEntity::getProcess)
            .collect(Collectors.toList());
        this.imageHeight = TEXTURE_HEIGHT;

        if(shaderManager == null) {
            try {
                shaderManager = new ShaderInstance(Minecraft.getInstance().getResourceManager(), ProjectNublar.MODID + ":incubator_bed", DefaultVertexFormat.POSITION_TEX);
            } catch (IOException e) {
                ProjectNublar.LOGGER.error("Unable to load incubator bed shader :/", e);
            }
        }
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);

        int left = this.leftPos + OVERLAY_START_X;
        int top = this.topPos + OVERLAY_START_Y;

        for (int i = 0; i < 9; i++) {
            Slot slotRaw = this.menu.getSlot(i + 1);
            if(slotRaw instanceof MachineModuleSlot) {
                MachineModuleSlot slot = (MachineModuleSlot) slotRaw;
                IncubatorBlockEntity.Egg egg = this.blockEntity.getEggList()[i];

                if (egg != null) {
                    int x = egg.getXPos() + left - this.leftPos - 8;
                    int y = egg.getYPos() + top - this.topPos - 8;
                    if(x != slot.x || y != slot.y) {
                        this.menu.slots.set(i + 1, slot = new MachineModuleSlot(this.blockEntity, slot.getSlotIndex(),x,y));
                        ((Slot) slot).index = slot.getSlotIndex();
                    }
                    slot.setActive(true);
                } else {
                    slot.setActive(false);
                }
            }
        }

        super.render(stack, mouseX, mouseY, partialTicks);

        for (int i = 0; i < 9; i++) {
            MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process = this.processes.get(i);
            IncubatorBlockEntity.Egg egg = this.blockEntity.getEggList()[i];
            if(egg != null) {
                this.drawProcessIcon(process, stack, OVERLAY_START_X+egg.getXPos() - 8, OVERLAY_START_Y+egg.getYPos() + 8);
            }
        }


        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int state) {
        int left = this.leftPos + OVERLAY_START_X;
        int top = this.topPos + OVERLAY_START_Y;
        int right = left + IncubatorBlockEntity.BED_WIDTH;
        int bottom = top + IncubatorBlockEntity.BED_HEIGHT;

        ItemStack stack = minecraft.player.getInventory().getSelected();
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
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, new ResourceLocation(ProjectNublar.MODID, "textures/gui/incubator.png"));
        stack.blit(new ResourceLocation(ProjectNublar.MODID, "textures/gui/incubator.png"), this.leftPos + OVERLAY_START_X, this.topPos + OVERLAY_START_Y, this.getOffset(), this.imageWidth, 0, IncubatorBlockEntity.BED_WIDTH, IncubatorBlockEntity.BED_HEIGHT, TEXTURE_HEIGHT, TEXTURE_WIDTH); //There is a vanilla bug that mixes up width and height

        float progress = this.blockEntity.getPlantMatter() / this.blockEntity.getTotalPlantMatter();

        RenderSystem.enableBlend();
        shaderManager.safeGetUniform("progress").set(progress);
        shaderManager.safeGetUniform("seed").set(this.blockEntity.getBlockPos().asLong());
        shaderManager.apply();

        int left = this.leftPos + OVERLAY_START_X;
        int top = this.topPos + OVERLAY_START_Y;
        int right = left + IncubatorBlockEntity.BED_WIDTH;
        int bottom = top + IncubatorBlockEntity.BED_HEIGHT;

        BufferBuilder buff = Tesselator.getInstance().getBuilder();
        buff.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buff.vertex(left, top, 0).uv(0, 0).endVertex();
        buff.vertex(left, bottom, 0).uv(0, 1).endVertex();
        buff.vertex(right, bottom, 0).uv(1, 1).endVertex();
        buff.vertex(right, top, 0).uv(1, 0).endVertex();

        Tesselator.getInstance().end();
        shaderManager.clear();

        ItemStack itemStack = Minecraft.getInstance().player.getInventory().getSelected();
        boolean holdingEgg = ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(itemStack.getItem());
        List<IncubatorBlockEntity.Egg> eggs = this.blockEntity.getCollidingEggs(mouseX - left, mouseY - top);
        if(mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom && holdingEgg) {
            stack.fill(left, top, right, bottom, eggs.isEmpty() ? 0x6600FF00 : 0x66FF0000);
        }
        if(holdingEgg) {
            for (IncubatorBlockEntity.Egg egg : eggs) {
                stack.fill(Math.max(left + egg.getXPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, left),
                    Math.max(top + egg.getYPos() - IncubatorBlockEntity.HALF_EGG_SIZE - IncubatorBlockEntity.EGG_PADDING, top),
                    Math.min(left + egg.getXPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, right),
                    Math.min(top + egg.getYPos() + IncubatorBlockEntity.HALF_EGG_SIZE + IncubatorBlockEntity.EGG_PADDING, bottom),
                    -1
                );
            }
        }


        stack.blit(new ResourceLocation(ProjectNublar.MODID, "textures/gui/incubator.png"), this.leftPos, this.topPos, this.getOffset(), 0, 0, this.imageWidth, this.imageHeight, TEXTURE_HEIGHT, TEXTURE_WIDTH); //There is a vanilla bug that mixes up width and height

        stack.fill(this.leftPos + 28, this.topPos + 121, (int) (this.leftPos + 28 + 63 * progress), this.topPos + 125, 0xFFA9E444);
    }
}
