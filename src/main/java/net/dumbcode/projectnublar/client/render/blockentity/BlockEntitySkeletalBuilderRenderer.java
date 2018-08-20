package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.model.InfoTabulaModel;
import net.dumbcode.projectnublar.client.render.MoreTabulaUtils;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.dumbcode.projectnublar.server.block.SkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;

import static java.lang.Math.PI;

public class BlockEntitySkeletalBuilderRenderer extends TileEntitySpecialRenderer<BlockEntitySkeletalBuilder> {
    private Minecraft mc = Minecraft.getMinecraft();
    @Override
    public void render(BlockEntitySkeletalBuilder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(SkeletalBuilder.FACING);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.pushMatrix();

        GlStateManager.enableLighting();
        Vector3f rotation = new Vector3f();
        float angle = 90F;
        switch (facing.getAxis()) {
            case X:
                rotation = new Vector3f(0, 0, 1);
                angle = facing == EnumFacing.WEST ? 90F : 270F;
                break;
            case Y:
                rotation = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.UP ? 0 : 180F;
                break;
            case Z:
                rotation = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.SOUTH ? 90F : 270F;
                break;
        }

        Matrix4d translateMatrix = new Matrix4d();
        Matrix4d rotateMatrix = new Matrix4d();
        translateMatrix.set(new Vector3d(te.getPos().getX(), te.getPos().getY() + 1, te.getPos().getZ()));

        if(facing != EnumFacing.UP) { //TODO: dosnt currently work with support poles
            GlStateManager.rotate(angle, rotation.x, rotation.y, rotation.z);
            if(rotation.x != 0) {
                rotateMatrix.rotX(angle);
            } else if(rotation.y != 0) {
                rotateMatrix.rotY(angle);
            } else if(rotation.z != 0) {
                rotateMatrix.rotZ(angle);
            }
        }
        GlStateManager.rotate(te.getRotation(), 0, 1, 0);
        rotateMatrix.rotY(te.getRotation() / 180D * PI);

        GlStateManager.translate(0f, 1f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        mc.getTextureManager().bindTexture(te.getDinosaur().getTextureLocation(te.getDinosaurEntity()));

        Map<String, Vector3f> poseData = te.getPoseData();


        if(te.getModel() != null) {
            for(ModelRenderer box : te.getModel().boxList) {
                Vector3f rotations = poseData.get(box.boxName);
                if(rotations != null) {
                    box.rotateAngleX = rotations.x;
                    box.rotateAngleY = rotations.y;
                    box.rotateAngleZ = rotations.z;
                }
            }
            DinosaurAnimator animator = ReflectionHelper.getPrivateValue(TabulaModel.class, te.getModel(), "tabulaAnimator");
            animator.setRotationAngles(te.getModel(), te.getDinosaurEntity(), 0f, 0f, 0f, 0f, 0f, 1f/16f);
            MoreTabulaUtils.renderModelWithoutChangingPose(te.getModel(), 1f/16f);
        }
        GlStateManager.popMatrix();

        List<String> anchoredParts = Lists.newArrayList("tail4", "tail2", "head", "chest"); //TODO: move to dinosaur class

        World world = te.getWorld();
        double poleLength = 0.1F;
        if(te.getModel() != null) {
            for (String anchoredPart : anchoredParts) {
                AdvancedModelRenderer cube = te.getModel().getCube(anchoredPart);
                if (cube != null && (cube.scaleX != 0 || cube.scaleY != 0 || cube.scaleZ != 0)) {
                    InfoTabulaModel infoModel = (InfoTabulaModel) te.getModel();
                    int[] dimensions = infoModel.getDimension(cube);
                    ModelBox box = ((List<ModelBox>)ReflectionHelper.getPrivateValue(ModelRenderer.class, cube, "cubeList")).get(0);


                    Point3d endPoint = new Point3d( (box.posX1 + dimensions[0] / 2F) / 16F, (box.posY1 + dimensions[1] / 2F) / -16F, (box.posZ1 + dimensions[2] / 2F) / -16F);

                    Matrix4d boxTranslate = new Matrix4d();
                    Matrix4d boxRotateX = new Matrix4d();
                    Matrix4d boxRotateY = new Matrix4d();
                    Matrix4d boxRotateZ = new Matrix4d();
                    boxTranslate.set(new Vector3d(cube.rotationPointX/16, -cube.rotationPointY/16, -cube.rotationPointZ/16));
                    boxRotateX.rotX(cube.rotateAngleX);
                    boxRotateY.rotY(-cube.rotateAngleY);
                    boxRotateZ.rotZ(-cube.rotateAngleZ);
                    boxRotateX.transform(endPoint);
                    boxRotateY.transform(endPoint);
                    boxRotateZ.transform(endPoint);
                    boxTranslate.transform(endPoint);

                    Vec3d partOrigin = cube.getModelPos(cube, new Vec3d(endPoint.x, endPoint.y, endPoint.z));
                    partOrigin = new Vec3d(-partOrigin.x, /*No need to minus the y, as we flip the model around anyway*/partOrigin.y, -partOrigin.z);

                    Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y, partOrigin.z);
                    rotateMatrix.transform(rendererPos);
                    translateMatrix.transform(rendererPos);

                    BlockPos sectionPos = new BlockPos(rendererPos.x + 0.5F, rendererPos.y + 0.5F, rendererPos.z + 0.5F);
                    List<AxisAlignedBB> aabbList = Lists.newArrayList();
                    while (aabbList.isEmpty() && sectionPos.getY() >= 0) {
                        world.getBlockState(sectionPos).addCollisionBoxToList(world, sectionPos, new AxisAlignedBB(rendererPos.x + 0.5F - poleLength / 2D, rendererPos.y + 0.5F, rendererPos.z + 0.5F - poleLength / 2D, rendererPos.x + 0.5F + poleLength / 2D, 0/*Maybe a better solution?*/, rendererPos.z + 0.5F + poleLength / 2D), aabbList, null, false);
                        sectionPos = sectionPos.down();
                    }
                    double yPos = 0;
                    if (!aabbList.isEmpty()) {
                        for (AxisAlignedBB aabb : aabbList) {
                            yPos = Math.max(0, aabb.maxY - 0.5F);
                        }
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    GlStateManager.color(1f, 1f, 1f, 1f);
                    Tessellator tes = Tessellator.getInstance();
                    BufferBuilder buff = tes.getBuffer();
                    buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
                    buff.pos(rendererPos.x, rendererPos.y, rendererPos.z).endVertex();
                    buff.pos(rendererPos.x, yPos, rendererPos.z).endVertex();
                    tes.draw();
                    GlStateManager.translate(rendererPos.x, yPos + 0.0001, rendererPos.z);
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
                    buff.pos(-0.1, 0, -0.1).endVertex();
                    buff.pos(-0.1, 0, 0.1).endVertex();
                    buff.pos(0.1, 0, 0.1).endVertex();
                    buff.pos(0.1, 0, -0.1).endVertex();

                    tes.draw();
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
    }
}
