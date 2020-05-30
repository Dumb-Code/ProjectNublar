package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.client.shader.GlslSandboxShader;
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
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class BlockEntityEggPrinterRenderer extends TileEntitySpecialRenderer<EggPrinterBlockEntity> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/egg_printer_animatable.tbl");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/egg_printer.png");

    private static final DinosaurEggType EGG_TYPE = EnumDinosaurEggTypes.ROUND.getType();
    private static final String NEEDLE_Z_MOVEMENT = "PrinterNeedleHolder";
    private static final int MOVEMENT_TICKS = 10;

    private static final Map<TabulaModel, TabulaModelClipPlane> MODEL_TO_CLIP_PLANE = new HashMap<>();

    private static final float[] TARGET_ANIMATION = new float[4];

    private TabulaModel model;


    public BlockEntityEggPrinterRenderer() {
        ((IReloadableResourceManager)MC.getResourceManager()).registerReloadListener(resourceManager -> this.model = TabulaUtils.getModel(MODEL_LOCATION));
    }

    @Override
    public void render(EggPrinterBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        this.setupLightmap(te.getPos());
        this.renderPrinterParts(te, partialTicks);

        GlStateManager.disableBlend();

        GlStateManager.popMatrix();

    }

    private void renderPrinterParts(EggPrinterBlockEntity te, float partialTicks) {

        GlStateManager.pushMatrix();
        this.doTabulaTransforms();

        GlStateManager.pushMatrix();
        MachineModuleBlockEntity.MachineProcess<?> process = te.getProcess(0);

        float recipeProgress = process.getTotalTime() == 0 ? 0 : (process.getTime() + partialTicks) / process.getTotalTime();
        boolean platformMove = process.getTotalTime() != 0;

        this.animateNeedle(te, partialTicks);
        this.animateLid(te, partialTicks);
        if(this.animatePlatform(te, platformMove, recipeProgress, partialTicks)) {
            platformMove = true;
            recipeProgress = 1F;
        }

        this.renderEgg(platformMove, recipeProgress*EGG_TYPE.getEggLength());

        this.applyAnimations();

        System.arraycopy(TARGET_ANIMATION, 0, te.getSnapshot(), 4, 4);

        this.bindTexture(TEXTURE_LOCATION);
        this.model.renderBoxes(1/16F);
        GlStateManager.enableBlend();
        this.bindTexture( new ResourceLocation(ProjectNublar.MODID, "textures/blocks/egg_printer_glass.png"));
        this.model.renderBoxes(1/16F);
        GlStateManager.popMatrix();


        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }

    private void applyAnimations() {
        TabulaModelRenderer platform = this.model.getCube("platform");
        platform.resetRotationPoint();
        platform.rotationPointY += TARGET_ANIMATION[0];

        TabulaModelRenderer xCube = this.model.getCube("printingRail1");
        xCube.resetRotationPoint();
        xCube.rotationPointX += TARGET_ANIMATION[1];

        TabulaModelRenderer zCube = this.model.getCube(NEEDLE_Z_MOVEMENT);
        zCube.resetRotationPoint();
        zCube.rotationPointZ += TARGET_ANIMATION[2];

        TabulaModelRenderer lidPart = this.model.getCube("lidrotatehelper");
        lidPart.resetRotations();
        lidPart.rotateAngleX += TARGET_ANIMATION[3];

    }

    private void animateLid(EggPrinterBlockEntity te, float partialTicks) {
        this.animatePart(te, !te.getOpenedUsers().isEmpty(), 2, 3, partialTicks, (float) (-60F * Math.PI/180F), 0);
    }

    private void animateNeedle(EggPrinterBlockEntity te, float partialTicks) {
        MachineModuleBlockEntity.MachineProcess<?> process = te.getProcess(0);

        float ticksPerRowX = 5;
        float ticksPerRowZ = 1;
        float rowX = (process.getTime()+partialTicks)/ticksPerRowX;
        float rowZ = (process.getTime()+partialTicks)/ticksPerRowZ;
        float size = 2;

        this.animatePart(
            te, process.isProcessing() && process.isHasPower() && !process.isFinished(), 1, 1, partialTicks,
            MathUtils.bounce(-1, 1, rowX)*size, MathUtils.bounce(-1, 1, rowZ)*size, 0, 0
        );
    }

    private boolean animatePlatform(EggPrinterBlockEntity te, boolean platformMove, float recipeProgress, float partialTicks) {
        MachineModuleBlockEntity.MachineProcess<?> process = te.getProcess(0);

        boolean ret = false;
        if(!platformMove) {
            ItemStack inSlot = te.getHandler().getStackInSlot(process.getOutputSlot(0));
            if(!inSlot.isEmpty() && (inSlot.getItem() == ItemHandler.ARTIFICIAL_EGG || inSlot.getItem() == ItemHandler.BROKEN_ARTIFICIAL_EGG)) {
                platformMove = true;
                recipeProgress = 1;
                ret = true;
            }
        }
        this.animatePart(te, platformMove, 0, 0, partialTicks, 16*recipeProgress*EGG_TYPE.getEggLength(), 4);
        return ret;
    }

    private void animatePart(EggPrinterBlockEntity te, boolean active, int stateID, int partOffset, float partialTicks, float... data) {
        float[] snapshot = te.getSnapshot();
        int length = data.length / 2;

        if(active) {
            System.arraycopy(data, 0, TARGET_ANIMATION, partOffset, length);
        } else {
            System.arraycopy(data, length, TARGET_ANIMATION, partOffset, length);
        }

        if(active != te.getPreviousStates()[stateID]) {
            te.getPreviousStates()[stateID] = active;
            for (int i = 0; i < length; i++) {
                snapshot[partOffset+i] = snapshot[4+partOffset+i];
            }
            te.getMovementTicksLeft()[stateID] = MOVEMENT_TICKS;
        } else if(te.getMovementTicksLeft()[stateID] > 0) {
            float perc = (te.getMovementTicksLeft()[stateID]-partialTicks)/MOVEMENT_TICKS;
            for (int i = 0; i < length; i++) {
                int id = partOffset+i;
                TARGET_ANIMATION[id] = TARGET_ANIMATION[id] + (snapshot[id] - TARGET_ANIMATION[id])*perc;
            }
        }

    }

    private void renderEgg(boolean doPlatform, float eggLength) {
        GlStateManager.pushMatrix();

        this.bindTexture(EGG_TYPE.getTexture());

        if(doPlatform) {
            GlStateManager.translate(0, -(24F-6.6F-2.75F)/16F + eggLength, 0);
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
