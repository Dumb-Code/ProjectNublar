package net.dumbcode.projectnublar.client.render.blockentity;

import lombok.Value;
import net.dumbcode.dumblibrary.client.BakedModelResolver;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class BlockEntityIncubatorRenderer extends TileEntitySpecialRenderer<IncubatorBlockEntity> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final float TICKS_TO_ROTATE = 20F;
    private static final float TICKS_WOBBLE = 15F;
    private static final float TICKS_TO_MOVE = 20F;
    private static final float TICKS_WAIT_BEFORE_SPIN = 10F;
    private static final float TICKS_TO_SPIN = 50F;
    private static final float TICKS_WAIT_AFTER_SPIN = 10F;
    private static final float TICKS_COOLDOWN = 20F;

    private static final ResourceLocation ARM_MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/incubator_arm.tbl");
    private static final ResourceLocation ARM_TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/incubator_arm.png");

    private static final BakedModelResolver LID_MODEL = new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_lid.tbl"));
    private static final BakedModelResolver TRANSLUCENT_LID_MODEL = new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_lid_trans.tbl"));
    private static final BakedModelResolver[] LIGHT_MODELS = IntStream.range(0, 4) //4 is the amount of incubator bulb upgrades + 1
        .mapToObj(i -> new BakedModelResolver(new ResourceLocation(ProjectNublar.MODID, "block/incubator_light_" + i + ".tbl")))
        .toArray(BakedModelResolver[]::new);

    private final Arm BASE_ROTATION = new Arm("ArmBase1", 3 / 16F);
    private final Arm FIRST_ARM = new Arm("Arm1", 8.5 / 16F);
    private final Arm LAST_ARM = new Arm("Arm2Base", 7 / 16F);
    private final Arm HAND_JOINT = new Arm("ClawNeck2", 4 / 16F);
    private final Arm HAND_JOINT_ROTATE = new Arm("ClawNeck1", 0); //Used for fixing parenting

    private TabulaModel armModel;



    public BlockEntityIncubatorRenderer() {
        ((IReloadableResourceManager)MC.getResourceManager()).registerReloadListener(resourceManager -> this.armModel = TabulaUtils.getModel(ARM_MODEL_LOCATION));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(IncubatorBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableCull();
        GlStateManager.enableAlpha();

        this.armModel.resetAnimations();

        float lidHeight = (float) (Math.sin(Math.PI * ((te.lidTicks[1] + (te.lidTicks[0] - te.lidTicks[1]) * partialTicks) / IncubatorBlockEntity.TICKS_TO_OPEN - 0.5D)) * 0.5D + 0.5D) * 12.5F/16F;

        int worldLight = this.getWorld().getCombinedLight(te.getPos().up(), 0);
        int blockLight = worldLight % 65536;
        int skyLight = worldLight / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, Math.max(blockLight, 7*16), skyLight);
        GlStateManager.color(1F, 1F, 1F, 1F);

        for (IncubatorBlockEntity.Egg egg : te.getEggList()) {
            if(egg != null) {
                this.renderEgg(egg);
            }
        }

        this.armModel.setOnRenderCallback(cube -> {
            float lightStart = -0.7F;

           Vec3d vec3d = TabulaUtils.getModelPosAlpha(cube, 0.5F, 0.5F, 0.5F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) MathHelper.clamp((-vec3d.x/lightStart + 1)*16*10, blockLight, 240F), skyLight);
        });

        this.renderIncubatorParts(te, partialTicks);

        GlStateManager.translate(0, lidHeight, 0);
        this.renderLid(te.getWorld(), te.getPos(), te.getTier(MachineModuleType.BULB));


        GlStateManager.popMatrix();
    }

    private void renderLid(World world, BlockPos pos, int bulbTier) {
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buff = tessellator.getBuffer();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        buff.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        MC.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, LID_MODEL.getModel(), world.getBlockState(pos), pos, buff, false);
        MC.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, LIGHT_MODELS[bulbTier].getModel(), world.getBlockState(pos), pos, buff, false);
        tessellator.draw();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        MC.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, TRANSLUCENT_LID_MODEL.getModel(), world.getBlockState(pos), pos, buff, false);
        buff.sortVertexData(0, 0, 0);
        tessellator.draw();

        buff.setTranslation(0, 0, 0);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderIncubatorParts(IncubatorBlockEntity te, float partialTicks) {
        GlStateManager.pushMatrix();
        this.doTabulaTransforms();

        this.setArmAngles(te.activeEgg[0] != -1 ? te.getEggList()[te.activeEgg[0]] : null, te.movementTicks + partialTicks, te.snapshot, te.activeEgg[1]);
        this.updateEgg(te, partialTicks);

        this.bindTexture(ARM_TEXTURE_LOCATION);
        GlStateManager.enableAlpha();
        this.armModel.renderBoxes(1/16F);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
    }


    private void renderEgg(IncubatorBlockEntity.Egg egg) {
        if(egg.getEggType() == DinosaurEggType.EMPTY) {
            return;
        }
        DinosaurEggType type = egg.getEggType();
        MC.renderEngine.bindTexture(type.getTexture());

        GlStateManager.pushMatrix();

        Vec3d eggEnd = egg.getEggPosition();
        Vec3d normal = egg.getPickupDirection().normalize();
        double eggRotationY = Math.atan2(normal.z, normal.x);
        double eggRotationZ = Math.atan2(normal.y, this.xzDistance(Vec3d.ZERO, normal));

        double eggLength = type.getEggLength();
        float scale = type.getScale()*2;

        GlStateManager.translate(eggEnd.x, eggEnd.y, eggEnd.z);

        GlStateManager.rotate((float) -Math.toDegrees(eggRotationY) + 180, 0, 1, 0);
        GlStateManager.rotate((float) -Math.toDegrees(eggRotationZ) + 180, 0, 0, 1);
        GlStateManager.rotate(-90, 0, 0, 1);

        GlStateManager.rotate((float) -Math.toDegrees(egg.getRotation()), 0, 1, 0);

        GlStateManager.scale(-scale, -scale, scale);
        if(ProjectNublar.DEBUG) {
            this.drawDebugLines(0, 0, 0);
        }
        GlStateManager.translate(0, eggLength*scale, 0);
        if(ProjectNublar.DEBUG) {
            this.drawDebugLines(0, 0, 0);
        }
        GlStateManager.translate(0, -1.5, 0);
        type.getEggModel().renderBoxes(1/16F);

        GlStateManager.popMatrix();
    }

    private void doTabulaTransforms() {
        GlStateManager.translate(0.5, 1.5, 0.5);
        GlStateManager.scale(-1F, -1F, 1F);
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
                .collect(IOCollectors.randomPicker(getWorld().rand))
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

        TabulaModelRenderer box = BASE_ROTATION.getBox();

        snapshot[0] = BASE_ROTATION.getBox().rotateAngleY;
        snapshot[1] = FIRST_ARM.getBox().rotateAngleZ;
        snapshot[2] = LAST_ARM.getBox().rotateAngleZ;
        snapshot[3] = HAND_JOINT_ROTATE.getBox().rotateAngleZ;
        snapshot[4] = HAND_JOINT_ROTATE.getBox().rotateAngleY;
        snapshot[5] = HAND_JOINT.getBox().rotateAngleZ;
        snapshot[6] = HAND_JOINT.getBox().rotateAngleY;
    }

    private void setArmAngles(@Nullable IncubatorBlockEntity.Egg egg, float movementTicks, float[] snapshot, int eggMoveAmount) {
        float rotateInterp = movementTicks / TICKS_TO_ROTATE;
        if(egg == null) {
            float movementInterp = (movementTicks - TICKS_TO_ROTATE) / TICKS_TO_MOVE;

            BASE_ROTATION.getBox().rotateAngleY = this.interpolate(snapshot[0], BASE_ROTATION.getBox().getDefaultRotation()[1], movementInterp);
            FIRST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[1], FIRST_ARM.getBox().getDefaultRotation()[2], rotateInterp);
            LAST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[2], LAST_ARM.getBox().getDefaultRotation()[2], rotateInterp);

            HAND_JOINT_ROTATE.getBox().rotateAngleZ = this.interpolate(snapshot[3], HAND_JOINT_ROTATE.getBox().getDefaultRotation()[2], rotateInterp);
            HAND_JOINT_ROTATE.getBox().rotateAngleY = this.interpolate(snapshot[4], HAND_JOINT_ROTATE.getBox().getDefaultRotation()[1] + Math.PI, rotateInterp);

            HAND_JOINT.getBox().rotateAngleZ = this.interpolate(snapshot[5], HAND_JOINT.getBox().getDefaultRotation()[2], rotateInterp);
            HAND_JOINT.getBox().rotateAngleY = this.interpolate(snapshot[6], HAND_JOINT.getBox().getDefaultRotation()[1], rotateInterp);

            if(movementTicks > TICKS_TO_MOVE + TICKS_TO_ROTATE) {
                BASE_ROTATION.getBox().rotateAngleY += this.wobble((movementTicks - TICKS_TO_MOVE - TICKS_TO_ROTATE) / TICKS_WOBBLE);
            }

            return;
        }
        float movementInterp = (movementTicks - TICKS_TO_ROTATE - TICKS_WOBBLE) / TICKS_TO_MOVE;

        if(eggMoveAmount > 1) {
            rotateInterp = movementInterp;
        }

        Vec3d origin = new Vec3d(1.5, 1.4, 0.5);
        Vec3d target = egg.getEggPosition();
        Vec3d normal = egg.getPickupDirection().normalize();
        Vec3d handJointTarget = target.add(normal.scale(HAND_JOINT.length));
        double baseYRotation = Math.atan2(handJointTarget.z - origin.z, handJointTarget.x - origin.x);
        Vec3d baseJoinTarget = new Vec3d(Math.cos(baseYRotation), 0, Math.sin(baseYRotation)).scale(BASE_ROTATION.length).add(origin);

        double xzlen = this.xzDistance(baseJoinTarget, handJointTarget);
        double angleFirstArmTriangle = this.cosineRule(LAST_ARM.length, FIRST_ARM.length, xzlen);
        double angleFirstArm = angleFirstArmTriangle + 1.5*Math.PI + Math.atan2(handJointTarget.y - baseJoinTarget.y, xzlen);
        double angleLastArm = this.cosineRule(xzlen, FIRST_ARM.length, LAST_ARM.length);

        //Usually this would be flipped, but because the model is also flipped, we flip it here.
        double handRotY = Math.atan2(-normal.x, -normal.z);
        double handRotZ = Math.atan2(-normal.y, this.xzDistance(Vec3d.ZERO, normal));

        if(ProjectNublar.DEBUG) {
            GlStateManager.popMatrix();
            this.drawDebugRenderers(origin, target, handJointTarget, baseYRotation, baseJoinTarget, angleFirstArm, angleLastArm);
            GlStateManager.pushMatrix();
            this.doTabulaTransforms();
        }

        BASE_ROTATION.getBox().rotateAngleY = this.interpolate(snapshot[0], baseYRotation, rotateInterp);
        FIRST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[1], angleFirstArm, movementInterp);
        LAST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[2], angleLastArm, movementInterp);

        //Make the hand face world down, meaning we can make it face the target much much easily as we don't have to worry about parented rotation.
        //The `handRotY` is used to rotate the whole hand to face the target.
        HAND_JOINT_ROTATE.getBox().rotateAngleZ = this.interpolate(snapshot[3], -(angleFirstArm + angleLastArm), movementInterp);
        HAND_JOINT_ROTATE.getBox().rotateAngleY = this.interpolate(snapshot[4], -(baseYRotation + handRotY + Math.PI/2D), movementInterp);

        HAND_JOINT.getBox().rotateAngleZ = this.interpolate(snapshot[5], -(handRotZ + Math.PI/2D), movementInterp);

        if(eggMoveAmount == 1 && movementTicks > TICKS_TO_ROTATE && movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE) {
            BASE_ROTATION.getBox().rotateAngleY += this.wobble((movementTicks - TICKS_TO_ROTATE) / TICKS_WOBBLE);
        }

        int directionModifier = ((eggMoveAmount % 2) * 2) - 1;
        if(movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE) {
            HAND_JOINT.getBox().rotateAngleY = this.interpolate(snapshot[6],directionModifier * Math.PI/2D, movementInterp);
        } else if(movementTicks > TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN){
            float spinTicks = (float) (Math.PI * (movementTicks - TICKS_TO_ROTATE - TICKS_WOBBLE - TICKS_TO_MOVE - TICKS_WAIT_BEFORE_SPIN)/TICKS_TO_SPIN);
            if(movementTicks < TICKS_TO_ROTATE + TICKS_WOBBLE + TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN + TICKS_TO_SPIN) {
                HAND_JOINT.getBox().rotateAngleY = (float) (directionModifier*Math.PI/2D + spinTicks);
                egg.setRotation(egg.getRotationStart() + spinTicks);
            } else {
                HAND_JOINT.getBox().rotateAngleY = (float) (directionModifier*Math.PI/2D + Math.PI);
            }
        } else {
            HAND_JOINT.getBox().rotateAngleY = (float) (directionModifier * Math.PI/2D);
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

    private double xzDistance(Vec3d from, Vec3d to) {
        return Math.sqrt((from.x - to.x)*(from.x - to.x) + (from.z - to.z)*(from.z - to.z));
    }

    private void drawDebugRenderers(Vec3d origin, Vec3d target, Vec3d handJointTarget, double baseYRotation, Vec3d baseJoinTarget, double angleFirstArm, double angleLastArm) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();

        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        buff.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(baseJoinTarget.x, baseJoinTarget.y, baseJoinTarget.z).color(0F, 1F, 0F, 1F).endVertex();
        buff.pos(handJointTarget.x, handJointTarget.y, handJointTarget.z).color(0F, 1F, 0F, 1F).endVertex();
        Tessellator.getInstance().draw();
        this.drawDebugLines(origin.x, origin.y, origin.z);
        this.drawDebugLines(target.x, target.y, target.z);
        this.drawDebugLines(baseJoinTarget.x, baseJoinTarget.y, baseJoinTarget.z);
        buff.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(target.x, target.y, target.z).color(0F, 1F, 0F, 1F).endVertex();
        buff.pos(handJointTarget.x, handJointTarget.y, handJointTarget.z).color(0F, 1F, 0F, 1F).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.translate(baseJoinTarget.x, baseJoinTarget.y, baseJoinTarget.z);
        GlStateManager.rotate((float) -Math.toDegrees(baseYRotation + Math.PI), 0, 1, 0);
        GlStateManager.rotate((float) -Math.toDegrees(angleFirstArm), 0, 0, 1);
        GlStateManager.translate(0, FIRST_ARM.length, 0);
        this.drawDebugLines(0, 0, 0);
        GlStateManager.rotate((float) -Math.toDegrees(angleLastArm + Math.PI), 0, 0, 1);
        GlStateManager.translate(0, LAST_ARM.length, 0);
        this.drawDebugLines(0, 0, 0);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
    }

    private void drawDebugLines(double x, double y, double z) {
        this.setLightmapDisabled(true);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1F, 1F, 1F);
        GlStateManager.glLineWidth(1F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translate(x, y, z);
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(0.3, 0, 0).color(1F, 0F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0).color(1F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0.3, 0).color(0F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0.3).color(0F, 0F, 1F, 0F).endVertex();
        buff.pos(0, 0, 0).color(0F, 0F, 1F, 1F).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableLighting();
        this.setLightmapDisabled(false);

        GlStateManager.popMatrix();
    }

    @Value
    private class Arm {
        String armName;
        double length;

        TabulaModelRenderer getBox() {
            return BlockEntityIncubatorRenderer.this.armModel.getCube(this.armName);
        }
    }
}
