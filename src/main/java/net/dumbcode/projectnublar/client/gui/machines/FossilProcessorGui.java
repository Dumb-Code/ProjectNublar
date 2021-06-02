package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FossilProcessorGui extends TabbedGuiContainer<MachineModuleContainer> {

    private final FluidTank tank;

    public FossilProcessorGui(MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar, FluidTank tank) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.tank = tank;
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        this.minecraft.textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        TextureAtlasSprite tas = this.minecraft.getBlockRenderer().getBlockModelShaper().getTexture(Blocks.WATER.defaultBlockState(), minecraft.level, BlockPos.ZERO); //TODO cache
        MachineUtils.drawTiledTexture(stack, this.leftPos + 8F, this.topPos + 8F + 52F * (1F - this.tank.getFluidAmount() / (float) this.tank.getCapacity()), this.leftPos + 24F, this.topPos + 58F, tas);

        minecraft.textureManager.bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        blit(stack, this.leftPos + 8, this.topPos + 8, 176, 0, 16, 102);

        int mX = mouseX - this.leftPos;
        int mY = mouseY - this.topPos;

        if(mX >= 8 && mX < 24 && mY >= 8 && mY < 59) {
            drawString(stack, minecraft.font, this.tank.getFluidAmount() + "mB / " + this.tank.getCapacity() + "mB", mouseX, mouseY, -1);
        }

        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        blit(stack, this.leftPos, this.topPos, 0, 0, this.width, this.height);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        this.minecraft.textureManager.bind(slotLocation);
        for(Slot slot : this.menu.slots) {
            blit(stack, this.leftPos + slot.x - 1, this.topPos + slot.y- 1, 7, 17, 18, 18);
        }
    }
}
