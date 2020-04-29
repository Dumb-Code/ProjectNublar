package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import lombok.Value;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BlockEntityIncubatorRenderer extends TileEntitySpecialRenderer<IncubatorBlockEntity> {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final float TICKS_TO_MOVE = 40F;
    private static final float TICKS_WAIT_BEFORE_SPIN = 20F;
    private static final float TICKS_TO_SPIN = 50F;
    private static final float TICKS_WAIT_AFTER_SPIN = 20F;
    private static final float TICKS_COOLDOWN = 50F;

    private static final ResourceLocation ARM_MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/incubator_arm.tbl");
    private static final ResourceLocation LID_LOCATION = new ResourceLocation(ProjectNublar.MODID, "block/incubator_lid.tbl");

    private final Arm BASE_ROTATION = new Arm("Neck1", 2.5 / 16F);
    private final Arm FIRST_ARM = new Arm("Arm1", 10.5 / 16F);
    private final Arm LAST_ARM = new Arm("Arm2Base", 8.5 / 16F);
    private final Arm HAND_JOINT = new Arm("ClawNeck2", 4 / 16F);
    private final Arm HAND_JOINT_ROTATE = new Arm("ClawNeck1", 0); //Used for fixing parenting

    private TabulaModel armModel;
    private IBakedModel lidModel;

    public BlockEntityIncubatorRenderer() {
        ((IReloadableResourceManager)MC.getResourceManager()).registerReloadListener(resourceManager -> this.armModel = TabulaUtils.getModel(ARM_MODEL_LOCATION));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        IModel imodel;
        try {
            imodel = ModelLoaderRegistry.getModel(LID_LOCATION);
        } catch (Exception e) {
            ProjectNublar.getLogger().error("Unable to get lid model at " + LID_LOCATION, e);
            imodel = ModelLoaderRegistry.getMissingModel();
        }
        this.lidModel = imodel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, location -> MC.getTextureMapBlocks().getAtlasSprite(location.toString()));
    }


    @Override
    public void render(IncubatorBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        this.renderLid(te.getWorld(), te.getPos());

        this.setupLightmap(te.getPos());

        this.renderArm(te, partialTicks);

        for (IncubatorBlockEntity.Egg egg : te.getEggList()) {
            if(egg != null) {
                this.renderEgg(egg);
            }
        }

        GlStateManager.popMatrix();
    }

    private void renderArm(IncubatorBlockEntity te, float partialTicks) {
        this.bindTexture(TextureMap.LOCATION_MISSING_TEXTURE);

        GlStateManager.pushMatrix();
        this.doTabulaTransforms();

        this.updateEgg(te, partialTicks);
        this.setAngles(te.activeEgg != -1 ? te.getEggList()[te.activeEgg] : null, te.movementTicks, te.snapshot, partialTicks);

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

    private void renderLid(World world, BlockPos pos) {
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 2, 0);
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

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buff.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
        MC.getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, this.lidModel, world.getBlockState(pos), pos, buff, false);
        buff.setTranslation(0, 0, 0);
        tessellator.draw();
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
        if(blockEntity.activeEgg != -1 && blockEntity.getEggList()[blockEntity.activeEgg] == null) {
            blockEntity.activeEgg = -1;
        }
        boolean doneSpin = blockEntity.movementTicks > TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN + TICKS_TO_SPIN + TICKS_WAIT_AFTER_SPIN;
        boolean foundNewEgg = false;
        if((blockEntity.activeEgg == -1 && blockEntity.movementTicks > TICKS_TO_MOVE + TICKS_COOLDOWN) || doneSpin) {
            //Get all the eggs that haven't been turned around for a minute
            foundNewEgg = IntStream.range(0, 9)
                .filter(i -> blockEntity.getEggList()[i] != null && blockEntity.getEggList()[i].getTicksSinceTurned() - TICKS_TO_MOVE - TICKS_WAIT_BEFORE_SPIN - TICKS_TO_SPIN > 1200)
                .boxed()
                .collect(IOCollectors.shuffler(getWorld().rand))
                .findAny()
                .map(i -> {
                    blockEntity.activeEgg = i;
                    blockEntity.movementTicks = 0;
                    blockEntity.getEggList()[i].setTicksSinceTurned(0);
                    this.createSnapshot(blockEntity);
                    return true;
                }).orElse(false);
        }
        if(doneSpin && !foundNewEgg) {
            blockEntity.activeEgg = -1;
            blockEntity.movementTicks = 0;
            this.createSnapshot(blockEntity);
        }

        blockEntity.movementTicks += partialTicks;
    }

    private void createSnapshot(IncubatorBlockEntity blockEntity) {
        float[] snapshot = blockEntity.snapshot;

        snapshot[0] = BASE_ROTATION.getBox().rotateAngleY;
        snapshot[1] = FIRST_ARM.getBox().rotateAngleZ;
        snapshot[2] = LAST_ARM.getBox().rotateAngleZ;
        snapshot[3] = HAND_JOINT_ROTATE.getBox().rotateAngleZ;
        snapshot[4] = HAND_JOINT_ROTATE.getBox().rotateAngleY;
        snapshot[5] = HAND_JOINT.getBox().rotateAngleZ;
        snapshot[6] = HAND_JOINT.getBox().rotateAngleY;
    }

    private void setAngles(@Nullable IncubatorBlockEntity.Egg egg, float movementTicks, float[] snapshot, float partialTicks) {
        float movementInterp = movementTicks / TICKS_TO_MOVE;
        if(egg == null) {
            BASE_ROTATION.getBox().rotateAngleY = this.interpolate(snapshot[0], BASE_ROTATION.getBox().getDefaultRotation()[1], movementInterp);
            FIRST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[1], FIRST_ARM.getBox().getDefaultRotation()[2], movementInterp);
            LAST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[2], LAST_ARM.getBox().getDefaultRotation()[2], movementInterp);

            HAND_JOINT_ROTATE.getBox().rotateAngleZ = this.interpolate(snapshot[3], HAND_JOINT_ROTATE.getBox().getDefaultRotation()[2], movementInterp);
            HAND_JOINT_ROTATE.getBox().rotateAngleY = this.interpolate(snapshot[4], HAND_JOINT_ROTATE.getBox().getDefaultRotation()[1] + Math.PI, movementInterp);


            HAND_JOINT.getBox().rotateAngleZ = this.interpolate(snapshot[5], HAND_JOINT.getBox().getDefaultRotation()[2], movementInterp);
            HAND_JOINT.getBox().rotateAngleY = this.interpolate(snapshot[6], HAND_JOINT.getBox().getDefaultRotation()[1], movementInterp);

            return;
        }

        Vec3d origin = new Vec3d(1.6, 1.4, 0.5);
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

        BASE_ROTATION.getBox().rotateAngleY = this.interpolate(snapshot[0], baseYRotation + Math.PI, movementInterp);
        FIRST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[1], angleFirstArm, movementInterp);
        LAST_ARM.getBox().rotateAngleZ = this.interpolate(snapshot[2], angleLastArm, movementInterp);

        //Make the hand face world down, meaning we can make it face the target much much easily as we don't have to worry about parented rotation.
        //The `handRotY` is used to rotate the whole hand to face the target.
        HAND_JOINT_ROTATE.getBox().rotateAngleZ = this.interpolate(snapshot[3], -(angleFirstArm + angleLastArm), movementInterp);
        HAND_JOINT_ROTATE.getBox().rotateAngleY = this.interpolate(snapshot[4], -(baseYRotation + handRotY + Math.PI/2D), movementInterp);

        HAND_JOINT.getBox().rotateAngleZ = this.interpolate(snapshot[5], -(handRotZ + Math.PI/2D), movementInterp);

        if(movementTicks < TICKS_TO_MOVE) {
            HAND_JOINT.getBox().rotateAngleY = this.interpolate(snapshot[6],Math.PI/2D, movementInterp);
        } else if(movementTicks > TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN){
            double delta = this.interpolate(Math.PI/2D, Math.PI, (movementTicks - TICKS_TO_MOVE - TICKS_WAIT_BEFORE_SPIN) / TICKS_TO_SPIN) - HAND_JOINT.getBox().rotateAngleY;
            if(movementTicks < TICKS_TO_MOVE + TICKS_WAIT_BEFORE_SPIN + TICKS_TO_SPIN) {
                HAND_JOINT.getBox().rotateAngleY += delta;
                egg.setRotation(egg.getRotation() + (float) delta);
            } else {
                HAND_JOINT.getBox().rotateAngleY = 0;
            }
        }
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
        return (float) (from + (to - from) * Math.min(partialTicks, 1));
    }

    private void setupLightmap(BlockPos pos) {
        RenderHelper.enableStandardItemLighting();
        int i = this.getWorld().getCombinedLight(pos.up(), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private double cosineRule(double opposite, double sideA, double sideB) {
        return Math.acos((sideA*sideA + sideB*sideB - opposite*opposite) / (2D * sideA * sideB));
    }

    private double xzDistance(Vec3d from, Vec3d to) {
        return Math.sqrt((from.x - to.x)*(from.x - to.x) + (from.z - to.z)*(from.z - to.z));
    }

    private void drawDebugRenderers(Vec3d origin, Vec3d target, Vec3d handJointTarget, double baseYRotation, Vec3d baseJoinTarget, double angleFirstArm, double angleLastArm) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();

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
    }

    private void drawDebugLines(double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(0.2, 0, 0).color(1F, 0F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0).color(1F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0.2, 0).color(0F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0.2).color(0F, 0F, 1F, 0F).endVertex();
        buff.pos(0, 0, 0).color(0F, 0F, 1F, 1F).endVertex();

        Tessellator.getInstance().draw();
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
