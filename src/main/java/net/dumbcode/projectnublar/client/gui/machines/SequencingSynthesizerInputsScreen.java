package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.SlotCanBeDisabled;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class SequencingSynthesizerInputsScreen extends SequencerSynthesizerBaseScreen {

    private final SequencingSynthesizerBlockEntity blockEntity;

    private final List<SlotCanBeDisabled> slots;
    private boolean inventoryOpen;

    public SequencingSynthesizerInputsScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, ITextComponent title, TabInformationBar bar, PlayerInventory playerInventory) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
        this.slots = inventorySlotsIn.disableSlots;

        for (SlotCanBeDisabled slot : this.slots) {
            slot.setActive(false);
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float state) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, state);
//        RenderSystem.color4f(1F, 1F, 1F, 1f);
//
//        this.setBlitOffset(100);
//        minecraft.textureManager.bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
//        blit(stack, this.leftPos + 21, this.topPos + 5, 176, 0, 16, 52);
//        blit(stack,this.leftPos + 59, this.topPos + 5, 176, 0, 16, 52);
//        blit(stack,this.leftPos + 97, this.topPos + 5, 176, 0, 16, 52);
//        blit(stack,this.leftPos + 135, this.topPos + 5, 176, 0, 16, 52);
//        this.setBlitOffset(0);
//
//        minecraft.textureManager.bind(PlayerContainer.BLOCK_ATLAS);
//        BlockModelShapes shapes = minecraft.getBlockRenderer().getBlockModelShaper();
//        RenderSystem.color4f(0.247058824F, 0.462745098F, 0.894117647F, 1F);
//        String waterTooltip = this.drawTankWithTooltip(stack, mouseX, mouseY, 21, (float) this.blockEntity.getTank().getFluidAmount() / this.blockEntity.getTank().getCapacity(), shapes.getTexture(Blocks.WATER.defaultBlockState(), minecraft.level, BlockPos.ZERO));
//
//        RenderSystem.color4f(1F, 1F, 1F, 1f);
//        String sugarTooltip = this.drawTankWithTooltip(stack, mouseX, mouseY, 59, this.blockEntity.getSugarAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.SUGAR_CANE.defaultBlockState(), minecraft.level, BlockPos.ZERO));
//        String boneTooltip = this.drawTankWithTooltip(stack, mouseX, mouseY, 97, this.blockEntity.getBoneAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.BONE_BLOCK.defaultBlockState(), minecraft.level, BlockPos.ZERO));
//
//        RenderSystem.color4f(0.0941F, 0.7098F, 0.2823F, 1f);
//        String plantTooltip = this.drawTankWithTooltip(stack, mouseX, mouseY, 135, this.blockEntity.getPlantAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.OAK_LEAVES.defaultBlockState(), minecraft.level, BlockPos.ZERO));
//
//        if(waterTooltip != null) {
//            drawString(stack, minecraft.font, waterTooltip, mouseX, mouseY, -1);
//        } else if(sugarTooltip != null) {
//            drawString(stack, minecraft.font, sugarTooltip, mouseX, mouseY, -1);
//        } else if(boneTooltip != null) {
//            drawString(stack, minecraft.font, boneTooltip, mouseX, mouseY, -1);
//        } else if(plantTooltip != null) {
//            drawString(stack, minecraft.font, plantTooltip, mouseX, mouseY, -1);
//        }
        this.renderTooltip(stack, mouseX, mouseY);
    }


    private String drawTankWithTooltip(MatrixStack stack, int mouseX, int mouseY, int left, double value, TextureAtlasSprite sprite) {
        MachineUtils.drawTiledTexture(stack, this.leftPos + left, (float) (this.topPos + 5F + 52F * (1F - value)), this.leftPos + left + 16F, this.topPos + 55F, sprite);
        if(mouseX > this.leftPos + left && mouseX < this.leftPos + left + 16F && mouseY > this.topPos + 5 && mouseY < this.topPos + 55F) {
            return Math.round(value * 100F) + "%";
        }
        return null;
    }
}
