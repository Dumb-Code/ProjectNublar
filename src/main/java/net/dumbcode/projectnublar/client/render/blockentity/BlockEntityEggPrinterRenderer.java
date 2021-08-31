package net.dumbcode.projectnublar.client.render.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.client.YRotatedModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.render.TabulaModelClipPlane;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public class BlockEntityEggPrinterRenderer extends TileEntityRenderer<EggPrinterBlockEntity> {

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/egg_printer_animatable.dcm");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/block/egg_printer.png");
    private static final ResourceLocation EGG_PRINTER_GLASS = new ResourceLocation(ProjectNublar.MODID, "textures/block/egg_printer_glass.png");

    private static final DinosaurEggType EGG_TYPE = EnumDinosaurEggTypes.ROUND.getType();
    private static final String NEEDLE_Z_MOVEMENT = "PrinterNeedleHolder";
    private static final int MOVEMENT_TICKS = 10;

    private static final Map<DCMModel, TabulaModelClipPlane> MODEL_TO_CLIP_PLANE = new HashMap<>();

    private static final float[] TARGET_ANIMATION = new float[4];

    private static DCMModel model;

    public BlockEntityEggPrinterRenderer(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(EggPrinterBlockEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light, int overlay) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        light = WorldRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());

        stack.pushPose();
        YRotatedModel.rotateStack(stack, te.getBlockState().getValue(MachineModuleBlock.FACING));

        stack.translate(0.5, 1.5, 0.5);
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));

        this.renderPrinterParts(te, buffers, stack, light, partialTicks);

        stack.popPose();
    }


    private void renderPrinterParts(EggPrinterBlockEntity te, IRenderTypeBuffer buffers, MatrixStack stack, int light, float partialTicks) {
        stack.pushPose();
        MachineModuleBlockEntity.MachineProcess<?> process = te.getProcess(0);

        ItemStack outSlot = te.getHandler().getStackInSlot(process.getOutputSlot(0));
        float recipeProgress = process.getTotalTime() == 0 ? 0 : (process.getTime() + partialTicks) / process.getTotalTime();
        boolean outputSlot = !outSlot.isEmpty() && (outSlot.getItem() == ItemHandler.ARTIFICIAL_EGG.get() || outSlot.getItem() == ItemHandler.BROKEN_ARTIFICIAL_EGG.get());
        boolean platformMove = process.getTotalTime() != 0;

        if(outputSlot) {
            recipeProgress = 1;
            platformMove = true;
        }

        this.animateNeedle(te, partialTicks);
        this.animateLid(te, partialTicks, outputSlot);
        this.animatePlatform(te, platformMove, recipeProgress, partialTicks);

        this.renderEgg(stack, buffers, light, platformMove, recipeProgress*EGG_TYPE.getEggLength());

        this.applyAnimations();

        System.arraycopy(TARGET_ANIMATION, 0, te.getSnapshot(), 4, 4);

        this.model.renderBoxes(stack, light, buffers, TEXTURE_LOCATION);
        RenderSystem.enableBlend();
        this.model.renderBoxes(stack, light, buffers, EGG_PRINTER_GLASS);

        stack.popPose();
    }

    private void applyAnimations() {
        DCMModelRenderer platform = this.model.getCube("platform");
        platform.resetRotationPoint();
        platform.y += TARGET_ANIMATION[0];

        DCMModelRenderer xCube = this.model.getCube("printingRail1");
        xCube.resetRotationPoint();
        xCube.x += TARGET_ANIMATION[1];

        DCMModelRenderer zCube = this.model.getCube(NEEDLE_Z_MOVEMENT);
        zCube.resetRotationPoint();
        zCube.z += TARGET_ANIMATION[2];

        DCMModelRenderer lidPart = this.model.getCube("lidrotatehelper");
        lidPart.resetRotations();
        lidPart.xRot += TARGET_ANIMATION[3];

    }

    private void animateLid(EggPrinterBlockEntity te, float partialTicks, boolean outputSlot) {
        this.animatePart(te, !te.getOpenedUsers().isEmpty() || outputSlot, 2, 3, partialTicks, (float) (60F * Math.PI/180F), 0);
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

    private void animatePlatform(EggPrinterBlockEntity te, boolean platformMove, float recipeProgress, float partialTicks) {
        this.animatePart(te, platformMove, 0, 0, partialTicks, 16*recipeProgress*EGG_TYPE.getEggLength(), 4);
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

    private void renderEgg(MatrixStack stack, IRenderTypeBuffer buffers, int light, boolean doPlatform, float eggLength) {
        stack.pushPose();
        if(doPlatform) {
            stack.translate(0, -(24F-6.6F-2.75F)/16F + eggLength, 0);
            MODEL_TO_CLIP_PLANE.computeIfAbsent(EGG_TYPE.getEggModel(), TabulaModelClipPlane::new).render(stack, light, EGG_TYPE.getTexture(), -1.5 + eggLength, 0xFFF7F1DD);
        } else {
            EGG_TYPE.getEggModel().renderBoxes(stack, light, buffers, EGG_TYPE.getTexture());
        }
        stack.popPose();
    }

    public static void onResourceManagerReload(IResourceManager resourceManager) {
        model = DCMUtils.getModel(MODEL_LOCATION);
    }
}
