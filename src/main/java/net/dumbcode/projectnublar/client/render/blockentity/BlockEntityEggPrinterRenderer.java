package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.render.TabulaModelClipPlane;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityEggPrinterRenderer extends TileEntitySpecialRenderer<EggPrinterBlockEntity> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation RAILS_MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/egg_printer_rails.tbl");
    private static final ResourceLocation RAILS_TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/egg_printer.png");

    private static final DinosaurEggType EGG_TYPE = EnumDinosaurEggTypes.ROUND.getType();
    private static final String NEEDLE_Z_MOVEMENT = "PrinterNeedleHolder";
    private static float NEEDLE_START_HEIGHT = (24F-6.6F-2F)/16F;
    private static float PLATFORM_START_HEIGHT = (24F-13.7F)/16F;

    private static final Map<TabulaModel, TabulaModelClipPlane> MODEL_TO_CLIP_PLANE = new HashMap<>();

    private TabulaModel railsModel;


    public BlockEntityEggPrinterRenderer() {
        ((IReloadableResourceManager)MC.getResourceManager()).registerReloadListener(resourceManager -> this.railsModel = TabulaUtils.getModel(RAILS_MODEL_LOCATION));
    }

    @Override
    public void render(EggPrinterBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        this.setupLightmap(te.getPos());

        this.renderRails(te, partialTicks);

        GlStateManager.popMatrix();

    }

    private void renderRails(EggPrinterBlockEntity te, float partialTicks) {
        this.bindTexture(RAILS_TEXTURE_LOCATION);

        GlStateManager.pushMatrix();
        this.doTabulaTransforms();

        GlStateManager.pushMatrix();
        MachineModuleBlockEntity.MachineProcess<?> process = te.getProcess(0);

        boolean active = process.isProcessing() && process.isHasPower() && !process.isFinished();
        boolean doPlatform = process.getTotalTime() != 0;
        float[] target = new float[3];
        float[] renderSnapshot = te.getRenderSnapshot();

        float f = process.getTotalTime() == 0 ? 0 : (process.getTime() + partialTicks) / process.getTotalTime();

        int moveTicks = 10;

        TabulaModelRenderer platform = this.railsModel.getCube("platform");
        if(!doPlatform) {
            ItemStack inSlot = te.getHandler().getStackInSlot(process.getOutputSlot(0));
            if(!inSlot.isEmpty() && (inSlot.getItem() == ItemHandler.ARTIFICIAL_EGG || inSlot.getItem() == ItemHandler.BROKEN_ARTIFICIAL_EGG)) {
                doPlatform = true;
                f = 1;
            }
        }
        if(doPlatform) {
            target[0] = 16*f*EGG_TYPE.getEggLength();
        } else {
            target[0] = 4;
        }
        if(doPlatform != te.getPreviousStates()[1]) {
            te.getPreviousStates()[1] = active;
            renderSnapshot[0] = te.getPrevSnapshot()[0];
            te.getMovementTicksLeft()[1] = moveTicks;
        } else if(te.getMovementTicksLeft()[1] > 0) {
            float perc = (te.getMovementTicksLeft()[1]-partialTicks)/moveTicks;
            target[0] = target[0] + (renderSnapshot[0] - target[0])*perc;
        }

        if(active) {
            float ticksPerRowX = 5;
            float ticksPerRowZ = 1;
            float rowX = (process.getTime()+partialTicks)/ticksPerRowX;
            float rowZ = (process.getTime()+partialTicks)/ticksPerRowZ;
            float size = 12F/16F;

            target[1] = MathUtils.bounce(-1, 1, rowX)*size;
            target[2] = MathUtils.bounce(-1, 1, rowZ)*size;
        }
        if(active != te.getPreviousStates()[0]) {
            te.getPreviousStates()[0] = active;
            renderSnapshot[1] = te.getPrevSnapshot()[1];
            renderSnapshot[2] = te.getPrevSnapshot()[2];
            te.getMovementTicksLeft()[0] = moveTicks;
        } else if(te.getMovementTicksLeft()[0] > 0) {
            float perc = (te.getMovementTicksLeft()[0]-partialTicks)/moveTicks;
            target[1] = target[1] + (renderSnapshot[1] - target[1])*perc;
            target[2] = target[2] + (renderSnapshot[2] - target[2])*perc;
        }

        platform.resetRotationPoint();
        platform.rotationPointY += target[0];

        TabulaModelRenderer xCube = this.railsModel.getCube("printingRail1");
        xCube.resetRotationPoint();
        xCube.rotationPointX += target[1];

        TabulaModelRenderer zCube = this.railsModel.getCube(NEEDLE_Z_MOVEMENT);
        zCube.resetRotationPoint();
        zCube.rotationPointZ += target[2];

        System.arraycopy(target, 0, te.getPrevSnapshot(), 0, 3);

        this.railsModel.renderBoxes(1/16F);
        GlStateManager.popMatrix();

        this.renderEgg(doPlatform, f*EGG_TYPE.getEggLength());

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }

    private void renderEgg(boolean doPlatform, float eggLength) {
        GlStateManager.pushMatrix();

        this.bindTexture(EGG_TYPE.getTexture());

        if(doPlatform) {
            GlStateManager.translate(0, -NEEDLE_START_HEIGHT + eggLength, 0);
            MODEL_TO_CLIP_PLANE.computeIfAbsent(EGG_TYPE.getEggModel(), TabulaModelClipPlane::new).render(-1.5 + eggLength, 0xFFF7F1DD);
        } else {
            EGG_TYPE.getEggModel().renderBoxes(1/16F);
        }

        GlStateManager.popMatrix();
    }


    private void doTabulaTransforms() {
        GlStateManager.translate(0.5, 1.5, 0.5);
        GlStateManager.scale(-1F, -1F, 1F);
    }

    private void setupLightmap(BlockPos pos) {
        RenderHelper.enableStandardItemLighting();
        int i = this.getWorld().getCombinedLight(pos.up(), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
