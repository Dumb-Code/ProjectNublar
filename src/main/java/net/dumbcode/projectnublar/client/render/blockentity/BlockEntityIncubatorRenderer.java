package net.dumbcode.projectnublar.client.render.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Value;
import net.dumbcode.dumblibrary.client.BakedModelResolver;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.dumbcode.studio.model.ModelMirror;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

public class BlockEntityIncubatorRenderer extends TileEntityRenderer<IncubatorBlockEntity> {

    private static final Minecraft MC = Minecraft.getInstance();

    private static final float TICKS_TO_ROTATE = 20F;
    private static final float TICKS_WOBBLE = 15F;
    private static final float TICKS_TO_MOVE = 20F;
    private static final float TICKS_WAIT_BEFORE_SPIN = 10F;
    private static final float TICKS_TO_SPIN = 50F;
    private static final float TICKS_WAIT_AFTER_SPIN = 10F;
    private static final float TICKS_COOLDOWN = 20F;

    private static final ResourceLocation ARM_MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/incubator_arm.dcm");
    private static final ResourceLocation ARM_TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/incubator_arm.png");

    private static final BakedModelResolver LID_MODEL = new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_lid"));
    private static final BakedModelResolver TRANSLUCENT_LID_MODEL = new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_lid_trans"));
    private static final BakedModelResolver[] LIGHT_MODELS = IntStream.range(0, 4) //4 is the amount of incubator bulb upgrades + 1
        .mapToObj(i -> new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_light_" + i)))
        .toArray(BakedModelResolver[]::new);

    private final Arm BASE_ROTATION = new Arm("ArmBase1", 3 / 16F);
    private final Arm FIRST_ARM = new Arm("Arm1", 8.5 / 16F);
    private final Arm LAST_ARM = new Arm("Arm2Base", 7 / 16F);
    private final Arm HAND_JOINT = new Arm("ClawNeck2", 4 / 16F);
    private final Arm HAND_JOINT_ROTATE = new Arm("ClawNeck1", 0); //Used for fixing parenting

    private static DCMModel armModel;

    public BlockEntityIncubatorRenderer(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(IncubatorBlockEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light, int overlay) {
        stack.pushPose();
        stack.translate(0.5, 0, 0.5);
        stack.mulPose(te.getBlockState().getValue(MachineModuleBlock.FACING).getRotation());
        stack.translate(-0.5, 0, -0.5);
        armModel.resetAnimations();

        float lidHeight = (float) (Math.sin(Math.PI * ((te.lidTicks[1] + (te.lidTicks[0] - te.lidTicks[1]) * partialTicks) / IncubatorBlockEntity.TICKS_TO_OPEN - 0.5D)) * 0.5D + 0.5D) * 12.5F/16F;

        BlockPos above = te.getBlockPos().above();
        int blockLight = te.getLevel().getBrightness(LightType.BLOCK, above);
        int skyLight = te.getLevel().getBrightness(LightType.SKY, above);
//        sky << 20 | block << 4
//        light = WorldRenderer.getLightColor(te.getLevel(), above);


        for (IncubatorBlockEntity.Egg egg : te.getEggList()) {
            if(egg != null) {
                this.renderEgg(egg, light, buffers, stack);
            }
        }

        armModel.setOnRenderCallback((cube, atomicLight, atomicOverlay, atomicColors) -> {
            float lightStart = -0.7F;
           Vector3f vec3f = DCMUtils.getModelPosAlpha(cube, 0.5F, 0.5F, 0.5F);
           int block = (int) MathHelper.clamp((-vec3f.x()/lightStart + 1)*10, blockLight, 15F);
            atomicLight.set(skyLight << 20 | block << 4);
        });

        this.renderIncubatorParts(te, stack, light, buffers, partialTicks);

        stack.translate(0, lidHeight, 0);
        this.renderLid(stack, te.getLevel(), te.getBlockPos(), buffers, te.getTier(MachineModuleType.BULB));


        stack.popPose();
    }

    private void renderLid(MatrixStack stack, World world, BlockPos pos, IRenderTypeBuffer buffers, int bulbTier) {
        IVertexBuilder buffer = buffers.getBuffer(RenderType.solid());
        BlockState blockState = world.getBlockState(pos);
        MC.getBlockRenderer().getModelRenderer().renderModel(world, LID_MODEL.getModel(), blockState, pos, stack, buffer, false, new Random(), blockState.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        MC.getBlockRenderer().getModelRenderer().renderModel(world, LIGHT_MODELS[bulbTier].getModel(), blockState, pos, stack, buffer, false, new Random(), blockState.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);

        buffer = buffers.getBuffer(RenderType.translucent());
        MC.getBlockRenderer().getModelRenderer().renderModel(world, TRANSLUCENT_LID_MODEL.getModel(), blockState, pos, stack, buffer, false, new Random(), blockState.getSeed(pos), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
    }

    private void renderIncubatorParts(IncubatorBlockEntity te, MatrixStack stack, int light, IRenderTypeBuffer buffers, float partialTicks) {
        this.setArmAngles(te.activeEgg[0] != -1 ? te.getEggList()[te.activeEgg[0]] : null, stack, buffers, te.movementTicks + partialTicks, te.snapshot, te.activeEgg[1]);
        this.updateEgg(te, partialTicks);

        armModel.renderBoxes(stack, light, buffers, ARM_TEXTURE_LOCATION);
    }


    private void renderEgg(IncubatorBlockEntity.Egg egg, int light, IRenderTypeBuffer buffers, MatrixStack stack) {
        if(egg.getEggType() == DinosaurEggType.EMPTY) {
            return;
        }
        DinosaurEggType type = egg.getEggType();

        stack.pushPose();

        Vector3d eggEnd = egg.getEggPosition();
        Vector3d normal = egg.getPickupDirection().normalize();
        double eggRotationY = Math.atan2(normal.z, normal.x);
        double eggRotationZ = Math.atan2(normal.y, this.xzDistance(Vector3d.ZERO, normal));

        double eggLength = type.getEggLength();
        float scale = type.getScale()*2;

        stack.translate(eggEnd.x, eggEnd.y, eggEnd.z);

        stack.mulPose(Vector3f.YP.rotation((float) (-eggRotationY + Math.PI)));
        stack.mulPose(Vector3f.ZP.rotation((float) (-eggRotationZ + Math.PI/2)));

        stack.mulPose(Vector3f.YP.rotation(egg.getRotation()));
        stack.scale(scale, scale, scale);

        IVertexBuilder lines = buffers.getBuffer(RenderType.lines());

        if(ProjectNublar.DEBUG) {
            this.drawDebugLines(stack, lines, 0, 0, 0);
        }
        stack.translate(0, eggLength*scale, 0);
        if(ProjectNublar.DEBUG) {
            this.drawDebugLines(stack, lines, 0, 0, 0);
        }
        stack.translate(0, -1.5, 0);
        type.getEggModel().renderBoxes(stack, light, buffers, type.getTexture());

        stack.popPose();
    }

    private void updateEgg(IncubatorBlockEntity blockEntity, float partialTicks) {
        for (IncubatorBlockEntity.Egg egg : blockEntity.getEggList()) {
            if(egg != null) {
                egg.setTicksSinceTurned(egg.getTicksSinceTurned() + partialTicks);
            }
        }
        if(blockEntity.activeEgg[0] != -1 && blockEntity.getEggList()[blockEntity.activeEgg[0]] == null) {
            blockEntity.activeEgg[0] = -1;
            blockEntity.activeEgg[1] = 0;
            blockEntity.movementTicks = 0;
            this.createSnapshot(blockEntity);
        }
        boolean doneSpin = blockEntity.movementTicks + partialTicks > TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN + TICKS_TO_SPIN + TICKS_WAIT_AFTER_SPIN;
        boolean foundNewEgg = false;
        if((blockEntity.activeEgg[0] == -1 && blockEntity.movementTicks + partialTicks > TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_COOLDOWN) || doneSpin) {
            //Get all the eggs that haven't been turned around for 5 minutes
            foundNewEgg = IntStream.range(0, 9)
                .filter(i ->
                    blockEntity.getEggList()[i] != null &&
                    blockEntity.getEggList()[i].getTicksSinceTurned() > 1200 /*6000*/ &&
                    blockEntity.getProcess(i).isHasPower() && blockEntity.getProcess(i).isProcessing()
                )
                .boxed()
                .collect(CollectorUtils.randomPicker(blockEntity.getLevel().random))
                .map(i -> {
                    blockEntity.activeEgg[1]++;
                    blockEntity.activeEgg[0] = i;
                    blockEntity.getEggList()[i].setRotationStart(blockEntity.getEggList()[i].getRotation());
                    blockEntity.movementTicks = blockEntity.activeEgg[1] == 1 ? 0 : TICKS_TO_ROTATE + TICKS_WOBBLE;
                    blockEntity.getEggList()[i].setTicksSinceTurned(0);
                    this.createSnapshot(blockEntity);
                    return true;
                }).orElse(false);
        }
        if(doneSpin && !foundNewEgg && blockEntity.activeEgg[0] != -1) {
            blockEntity.activeEgg[0] = -1;
            blockEntity.activeEgg[1] = 0;
            blockEntity.movementTicks = 0;
            this.createSnapshot(blockEntity);
        }
    }

    private void createSnapshot(IncubatorBlockEntity blockEntity) {
        float[] snapshot = blockEntity.snapshot;

        DCMModelRenderer box = BASE_ROTATION.getBox();

        snapshot[0] = BASE_ROTATION.getBox().yRot;
        snapshot[1] = FIRST_ARM.getBox().zRot;
        snapshot[2] = LAST_ARM.getBox().zRot;
        snapshot[3] = HAND_JOINT_ROTATE.getBox().zRot;
        snapshot[4] = HAND_JOINT_ROTATE.getBox().yRot;
        snapshot[5] = HAND_JOINT.getBox().zRot;
        snapshot[6] = HAND_JOINT.getBox().yRot;
    }

    private void setArmAngles(@Nullable IncubatorBlockEntity.Egg egg, MatrixStack stack, IRenderTypeBuffer buffers, float movementTicks, float[] snapshot, int eggMoveAmount) {
        float rotateInterp = movementTicks / TICKS_TO_ROTATE;
        if(egg == null) {
            float movementInterp = (movementTicks - TICKS_TO_ROTATE) / TICKS_TO_MOVE;

            BASE_ROTATION.getBox().yRot = this.interpolate(snapshot[0], BASE_ROTATION.getRotation(1), movementInterp);
            FIRST_ARM.getBox().zRot = this.interpolate(snapshot[1], FIRST_ARM.getRotation(2), rotateInterp);
            LAST_ARM.getBox().zRot = this.interpolate(snapshot[2], LAST_ARM.getRotation(2), rotateInterp);

            HAND_JOINT_ROTATE.getBox().zRot = this.interpolate(snapshot[3], HAND_JOINT_ROTATE.getRotation(2), rotateInterp);
            HAND_JOINT_ROTATE.getBox().yRot = this.interpolate(snapshot[4], HAND_JOINT_ROTATE.getRotation(1) + Math.PI, rotateInterp);

            HAND_JOINT.getBox().zRot = this.interpolate(snapshot[5], HAND_JOINT.getRotation(2), rotateInterp);
            HAND_JOINT.getBox().yRot = this.interpolate(snapshot[6], HAND_JOINT.getRotation(1), rotateInterp);

            if(movementTicks > TICKS_TO_MOVE + TICKS_TO_ROTATE) {
                BASE_ROTATION.getBox().yRot += this.wobble((movementTicks - TICKS_TO_MOVE - TICKS_TO_ROTATE) / TICKS_WOBBLE);
            }

            return;
        }
        float movementInterp = (movementTicks - TICKS_TO_ROTATE - TICKS_WOBBLE) / TICKS_TO_MOVE;

        if(eggMoveAmount > 1) {
            rotateInterp = movementInterp;
        }

        Vector3d origin = new Vector3d(1.5, 1.4, 0.5);
        Vector3d target = egg.getEggPosition();
        Vector3d normal = egg.getPickupDirection().normalize();
        Vector3d handJointTarget = target.add(normal.scale(HAND_JOINT.length));
        double baseYRotation = Math.atan2(handJointTarget.z - origin.z, handJointTarget.x - origin.x);
        Vector3d baseJoinTarget = new Vector3d(Math.cos(baseYRotation), 0, Math.sin(baseYRotation)).scale(BASE_ROTATION.length).add(origin);

        double xzlen = this.xzDistance(baseJoinTarget, handJointTarget);
        double angleFirstArmTriangle = this.cosineRule(LAST_ARM.length, FIRST_ARM.length, xzlen);
        double angleFirstArm = angleFirstArmTriangle + 1.5*Math.PI + Math.atan2(handJointTarget.y - baseJoinTarget.y, xzlen);
        double angleLastArm = this.cosineRule(xzlen, FIRST_ARM.length, LAST_ARM.length);

        //Usually this would be flipped, but because the model is also flipped, we flip it here.
        double handRotY = Math.atan2(-normal.x, -normal.z);
        double handRotZ = Math.atan2(-normal.y, this.xzDistance(Vector3d.ZERO, normal));

        if(ProjectNublar.DEBUG) {
            stack.popPose();
            this.drawDebugRenderers(stack, buffers, origin, target, handJointTarget, baseYRotation, baseJoinTarget, angleFirstArm, angleLastArm);
            stack.pushPose();
            //doTabulaTransforms
        }

        BASE_ROTATION.getBox().yRot = this.interpolate(snapshot[0], baseYRotation, rotateInterp);
        FIRST_ARM.getBox().zRot = this.interpolate(snapshot[1], angleFirstArm, movementInterp);
        LAST_ARM.getBox().zRot = this.interpolate(snapshot[2], angleLastArm, movementInterp);

        //Make the hand face world down, meaning we can make it face the target much much easily as we don't have to worry about parented rotation.
        //The `handRotY` is used to rotate the whole hand to face the target.
        HAND_JOINT_ROTATE.getBox().zRot = this.interpolate(snapshot[3], -(angleFirstArm + angleLastArm), movementInterp);
        HAND_JOINT_ROTATE.getBox().yRot = this.interpolate(snapshot[4], -(baseYRotation + handRotY + Math.PI/2D), movementInterp);

        HAND_JOINT.getBox().zRot = this.interpolate(snapshot[5], -(handRotZ + Math.PI/2D), movementInterp);

        if(eggMoveAmount == 1 && movementTicks > TICKS_TO_ROTATE && movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE) {
            BASE_ROTATION.getBox().yRot += this.wobble((movementTicks - TICKS_TO_ROTATE) / TICKS_WOBBLE);
        }

        int directionModifier = ((eggMoveAmount % 2) * 2) - 1;
        if(movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE) {
            HAND_JOINT.getBox().yRot = this.interpolate(snapshot[6],directionModifier * Math.PI/2D, movementInterp);
        } else if(movementTicks > TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN){
            float spinTicks = (float) (Math.PI * (movementTicks - TICKS_TO_ROTATE - TICKS_WOBBLE - TICKS_TO_MOVE - TICKS_WAIT_BEFORE_SPIN)/TICKS_TO_SPIN);
            if(movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN + TICKS_TO_SPIN) {
                HAND_JOINT.getBox().yRot = (float) (directionModifier*Math.PI/2D + spinTicks);
                egg.setRotation(egg.getRotationStart() + spinTicks);
            } else {
                HAND_JOINT.getBox().yRot = (float) (directionModifier*Math.PI/2D + Math.PI);
            }
        } else {
            HAND_JOINT.getBox().yRot = (float) (directionModifier * Math.PI/2D);
        }
    }

    private float wobble(float interp) {
        return (float) (Math.PI/30 * Math.sin(8*interp*Math.PI) * Math.max((0.5-interp), 0));
    }

    private float interpolate(double from, double to, double partialTicks) {
        //Make sure the degrees are from 0 to 360
        while(from < 0) {
            from += 2*Math.PI;
        }
        from %= 2*Math.PI;

        while(to < 0) {
            to += 2*Math.PI;
        }
        to %= 2*Math.PI;

        //This is to fix the issue that euler angles interpolation causes.
        //Interpolating from 1 degree to 359 degrees is 2 degrees visually, but without
        //wrapping it around would cause the interpolation to take the long way around
        if(Math.abs(from - to) > Math.PI) {
            if(from < to) {
                from += 2*Math.PI;
            } else {
                to += 2*Math.PI;
            }
        }

        //If `to` or `from` are NAN, then the interpolation will return a NAN. This is to make sure that doesn't happen
        if(partialTicks >= 1) {
            return (float) to;
        }
        if(partialTicks <= 0) {
            return (float) from;
        }
        return (float) (from + (to - from) * partialTicks);
    }

    private double cosineRule(double opposite, double sideA, double sideB) {
        return Math.acos((sideA*sideA + sideB*sideB - opposite*opposite) / (2D * sideA * sideB));
    }

    private double xzDistance(Vector3d from, Vector3d to) {
        return Math.sqrt((from.x - to.x)*(from.x - to.x) + (from.z - to.z)*(from.z - to.z));
    }

    private void drawDebugRenderers(MatrixStack stack, IRenderTypeBuffer buffer, Vector3d origin, Vector3d target, Vector3d handJointTarget, double baseYRotation, Vector3d baseJoinTarget, double angleFirstArm, double angleLastArm) {
        IVertexBuilder buff = buffer.getBuffer(RenderType.lines());

        Matrix4f pose = stack.last().pose();
        buff.vertex(pose, (float) baseJoinTarget.x, (float) baseJoinTarget.y, (float) baseJoinTarget.z).color(0F, 1F, 0F, 1F).endVertex();
        buff.vertex(pose, (float) handJointTarget.x, (float) handJointTarget.y, (float) handJointTarget.z).color(0F, 1F, 0F, 1F).endVertex();

        this.drawDebugLines(stack, buff, origin.x, origin.y, origin.z);
        this.drawDebugLines(stack, buff, target.x, target.y, target.z);
        this.drawDebugLines(stack, buff, baseJoinTarget.x, baseJoinTarget.y, baseJoinTarget.z);

        buff.vertex(pose, (float) target.x, (float) target.y, (float) target.z).color(0F, 1F, 0F, 1F).endVertex();
        buff.vertex(pose, (float) handJointTarget.x, (float) handJointTarget.y, (float) handJointTarget.z).color(0F, 1F, 0F, 1F).endVertex();

        stack.translate(baseJoinTarget.x, baseJoinTarget.y, baseJoinTarget.z);
        stack.mulPose(Vector3f.YP.rotation((float) (-baseYRotation - Math.PI)));
        stack.mulPose(Vector3f.ZP.rotation((float) -angleFirstArm));
        stack.translate(0, FIRST_ARM.length, 0);
        this.drawDebugLines(stack, buff, 0, 0, 0);
        stack.mulPose(Vector3f.ZP.rotation((float) (-angleLastArm - Math.PI)));
        stack.translate(0, LAST_ARM.length, 0);
        this.drawDebugLines(stack, buff, 0, 0, 0);
        stack.popPose();
    }

    private void drawDebugLines(MatrixStack stack, IVertexBuilder buff, double x, double y, double z) {
        stack.pushPose();
        stack.translate(x, y, z);
        Matrix4f pose = stack.last().pose();
        buff.vertex(pose, 0, 0, 0).color(1F, 0F, 0F, 1F).endVertex();
        buff.vertex(pose, 0.3F, 0, 0).color(1F, 0F, 0F, 1F).endVertex();
        buff.vertex(pose, 0, 0, 0).color(0F, 1F, 0F, 1F).endVertex();
        buff.vertex(pose, 0, 0.3F, 0).color(0F, 1F, 0F, 1F).endVertex();
        buff.vertex(pose, 0, 0, 0).color(0F, 0F, 1F, 1F).endVertex();
        buff.vertex(pose, 0, 0, 0.3F).color(0F, 0F, 1F, 1F).endVertex();
        stack.popPose();
    }

    @Value
    private static class Arm {
        String armName;
        double length;

        DCMModelRenderer getBox() {
            return BlockEntityIncubatorRenderer.armModel.getCube(this.armName);
        }

        float getRotation(int index) {
            return this.getBox()
                .getInfo()
                .getRotation()[index];
        }
        
    }

    public static void onResourceManagerReload(IResourceManager resourceManager) {
        armModel = DCMUtils.getModel(ARM_MODEL_LOCATION);
    }
}
