package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FossilProcessorScreen extends MachineContainerScreen {

    private final FluidTank tank;
    private final MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process;

    public FossilProcessorScreen(MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar, FluidTank tank, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.tank = tank;
        this.process = process;
        this.imageHeight = 217;
        this.titleLabelX = 6;
        this.titleLabelY = 3;
        this.inventoryLabelX = 6;
        this.inventoryLabelY = 114;
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        this.minecraft.textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        RenderSystem.color4f(63F / 255F, 118F / 255F, 228F / 255F, 1F);
        TextureAtlasSprite tas = this.minecraft.getBlockRenderer().getBlockModelShaper().getTexture(Blocks.WATER.defaultBlockState(), minecraft.level, BlockPos.ZERO); //TODO cache
        MachineUtils.drawTiledTexture(stack, this.leftPos + 22F, this.topPos + 56F + 48F * (1F - this.tank.getFluidAmount() / (float) this.tank.getCapacity()), this.leftPos + 38F, this.topPos + 104F, tas);
        RenderSystem.color4f(1F, 1F, 1F, 1F);

        minecraft.textureManager.bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        blit(stack, this.leftPos + 22, this.topPos + 56, 176, 0, 16, 48);
        subPixelBlit(stack, this.leftPos + 82, this.topPos + 35, 176, 49, 11, this.process.getTimeDone() * 19F);

        int mX = mouseX - this.leftPos;
        int mY = mouseY - this.topPos;

        if(mX >= 22 && mX < 22+16 && mY >= 56 && mY < 56+48) {
            drawString(stack, minecraft.font, this.tank.getFluidAmount() + "mB / " + this.tank.getCapacity() + "mB", mouseX, mouseY, -1);
        }

        drawProcessTooltip(this.process, stack, 80, 34, 15, 22, mouseX, mouseY);
        drawProcessIcon(this.process, stack, 79.5F, 37);

        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
