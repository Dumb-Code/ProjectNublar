package net.dumbcode.projectnublar.client.render.blockentity;

import lombok.Value;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.Language;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class BlockEntityIncubatorRenderer extends TileEntitySpecialRenderer<IncubatorBlockEntity> {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final ResourceLocation ARM_MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/incubator_arm.tbl");

    private final Arm BASE_ROTATION = new Arm("Neck1", 2.5 / 16F);
    private final Arm FIRST_ARM = new Arm("Arm1", 7 / 16F);
    private final Arm LAST_ARM = new Arm("Arm2Base", 6.5 / 16F);
    private final Arm HAND = new Arm("ClawNeck1", 4 / 16F);

    private TabulaModel armModel;

    public BlockEntityIncubatorRenderer() {
        ((IReloadableResourceManager)MC.getResourceManager()).registerReloadListener(resourceManager -> this.armModel = TabulaUtils.getModel(ARM_MODEL_LOCATION));
    }

    @Override
    public void render(IncubatorBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        this.setLightmapDisabled(true);
        this.bindTexture(TextureMap.LOCATION_MISSING_TEXTURE);

        Vec3d origin = new Vec3d(1.6, 1.4, 0.5);

        Vec3d target = new Vec3d(2.4 + (Math.sin((te.getWorld().getTotalWorldTime() + partialTicks) / 25D)/5D), 1.3 + (Math.cos((te.getWorld().getTotalWorldTime() + partialTicks) / 17D)/5D), 0.5 + (Math.cos((te.getWorld().getTotalWorldTime() + partialTicks) / 22D)/3D));
        Vec3d normal = new Vec3d(-1, 0, -1).normalize();

        Vec3d handJointTarget = target.add(normal.scale(HAND.length));
        double baseYRotation = Math.atan2(handJointTarget.z - origin.z, handJointTarget.x - origin.x);
        Vec3d baseJoinTarget = new Vec3d(Math.cos(baseYRotation), 0, Math.sin(baseYRotation)).scale(BASE_ROTATION.length).add(origin);

        Vec3d change = handJointTarget.subtract(baseJoinTarget);
        double xzlen = Math.sqrt(change.x*change.x + change.z*change.z);
        double angleFirstArmTriangle = this.cosineRule(LAST_ARM.length, FIRST_ARM.length, xzlen);
        double angleFirstArm = angleFirstArmTriangle + 1.5*Math.PI + Math.atan2(change.y, xzlen);
        double angleLastArm = this.cosineRule(xzlen, FIRST_ARM.length, LAST_ARM.length);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 2, z);

        this.drawDebugRenderers(origin, target, handJointTarget, baseYRotation, baseJoinTarget, angleFirstArm, angleLastArm);

        GlStateManager.translate(0.5, 1.5, 0.5);
        GlStateManager.scale(-1F, -1F, 1F);

        BASE_ROTATION.getBox().rotateAngleY = (float) (baseYRotation + Math.PI);
        FIRST_ARM.getBox().rotateAngleZ = (float) (angleFirstArm);
        LAST_ARM.getBox().rotateAngleZ = (float) (angleLastArm);

        this.armModel.renderBoxes(1/16F);

        GlStateManager.popMatrix();
    }

    private double cosineRule(double opposite, double sideA, double sideB) {
        return Math.acos((sideA*sideA + sideB*sideB - opposite*opposite) / (2D * sideA * sideB));
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
        this.drawDebugLines(0, LAST_ARM.length, 0);
        GlStateManager.popMatrix();
    }

    private void drawDebugLines(double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(0.5, 0, 0).color(1F, 0F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0).color(1F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0.5, 0).color(0F, 1F, 0F, 1F).endVertex();
        buff.pos(0, 0, 0.5).color(0F, 0F, 1F, 0F).endVertex();
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
