package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.projectnublar.client.render.MoreTabulaUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.SkeletalBuilderBlock;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.impl.SkeletalBuilderComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Math.PI;

public class BlockEntitySkeletalBuilderRenderer extends TileEntitySpecialRenderer<SkeletalBuilderBlockEntity> {
    private Minecraft mc = Minecraft.getMinecraft();



    @Override
    //TODO: split this into several methods, so solarlint doesn't report this as having a 102 cognitive complexity
    public void render(SkeletalBuilderBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        Optional<DinosaurEntity> optionalDinosaurEntity = te.getDinosaurEntity();
        if(!optionalDinosaurEntity.isPresent()) {
            return;
        }
        DinosaurEntity entity = optionalDinosaurEntity.get();


        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if(state.getBlock() != BlockHandler.SKELETAL_BUILDER) { //Can sometimes happen when loading in a save. Not sure why, but it happens
            return;
        }
        GlStateManager.pushMatrix();
        EnumFacing facing = state.getValue(SkeletalBuilderBlock.FACING);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        GlStateManager.pushMatrix();
        BufferBuilder buff = Tessellator.getInstance().getBuffer();

        GlStateManager.enableAlpha();

        Vector3f rotVec = new Vector3f();
        float angle = 90F;
        switch (facing.getAxis()) { //There gotta be a better way than this
            case X:
                rotVec = new Vector3f(0, 0, 1);
                angle = facing == EnumFacing.WEST ? 90F : 270F;
                break;
            case Y:
                rotVec = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.UP ? 0 : 180F;
                break;
            case Z:
                rotVec = new Vector3f(1, 0, 0);
                angle = facing == EnumFacing.SOUTH ? 90F : 270F;
                break;
        }

        Matrix4d translateMatrix = new Matrix4d();
        Matrix4d rotateMatrix = new Matrix4d();
        Matrix4d facingMatrix = new Matrix4d();

        Vec3i facingRot = facing.getDirectionVec();
        translateMatrix.set(new Vector3d(te.getPos().getX() - facingRot.getX() / 2F, te.getPos().getY() - facingRot.getY() / 2F, te.getPos().getZ() - facingRot.getZ() / 2F));

        GlStateManager.rotate(angle, rotVec.x, rotVec.y, rotVec.z);
        if(rotVec.x != 0) {
            facingMatrix.rotX(angle / 180F * Math.PI);
        } else if(rotVec.y != 0) {
            facingMatrix.rotY(angle / 180F * Math.PI);
        } else if(rotVec.z != 0) {
            facingMatrix.rotZ(angle / 180F * Math.PI);
        }

        float teRot = te.getSkeletalProperties().getRotation();

        teRot = teRot + (te.getSkeletalProperties().getPrevRotation() - teRot) * partialTicks;
        GlStateManager.rotate(teRot, 0, 1, 0);
        rotateMatrix.rotY(teRot / 180D * PI);

        GlStateManager.translate(0, -0.5F, 0);
        entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> GlStateManager.scale(floats[0],floats[1],floats[2]));
        GlStateManager.translate(0f, 1.5f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        mc.getTextureManager().bindTexture(te.getTexture());

        Map<String, Vector3f> poseData = te.getPoseData();

        GlStateManager.disableCull();

        TabulaModel model = te.getModel();

        if(model != null) {
            for(TabulaModelRenderer box : model.getAllCubes()) {
                Vector3f rotations = poseData.get(box.boxName);
                if(rotations != null) {
                    box.rotateAngleX = rotations.x;
                    box.rotateAngleY = rotations.y;
                    box.rotateAngleZ = rotations.z;
                } else {
                    box.resetRotations();
                }
            }

            setVisability(entity, model);
            model.renderBoxes(1F/16F);
            resetVisability(model);

        }
        GlStateManager.popMatrix();

        GlStateManager.enableCull();

        World world = te.getWorld();

        double poleWidth = 1 / 16F;
        double baseWidth = 6 / 16F;
        double baseHeight = 1 / 16F;

        double notchWidth = 4 / 16F;
        double notchHeight = 2 / 16F;

        float textureWidth = 8F;
        float textureHeight = 16F;

        if (model != null) {
            for (SkeletalProperties.Pole pole : te.getSkeletalProperties().getPoles()) {
                String anchoredPart = pole.getCubeName();
                PoleFacing poleDirection = pole.getFacing();
                EnumFacing poleFacing = poleDirection.getFacing();

                TabulaModelRenderer cube = model.getCube(anchoredPart);
                if (poleDirection != PoleFacing.NONE && cube != null && (cube.getScaleX() != 0 || cube.getScaleY() != 0 || cube.getScaleZ() != 0)) {
                    Vec3d partOrigin = TabulaUtils.getModelPosAlpha(cube, 0.5F, 0.5F, 0.5F);
                    partOrigin = new Vec3d(-partOrigin.x, /*No need to minus the y, as we flip the model around anyway*/partOrigin.y, -partOrigin.z);

                    Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y + 1.5, partOrigin.z);

                    entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> {
                        rendererPos.x *= floats[0];
                        rendererPos.y *= floats[1];
                        rendererPos.z *= floats[2];

                    });
                    rotateMatrix.transform(rendererPos);
                    facingMatrix.transform(rendererPos);
                    translateMatrix.transform(rendererPos);

                    AxisAlignedBB bounding = new AxisAlignedBB(0, 0, 0, poleFacing.getXOffset() * 256F, poleFacing.getYOffset() * 256F, poleFacing.getZOffset() * 256F)
                                    .offset(rendererPos.x + 0.5F, rendererPos.y + 0.5F, rendererPos.z + 0.5F)
                                    .grow(baseWidth / 2F);


                    double renderPosition;
                    switch (poleFacing.getAxis()) {
                        case X:
                            renderPosition = rendererPos.x;
                            break;
                        case Y:
                            renderPosition = rendererPos.y;
                            break;
                        case Z:
                            renderPosition = rendererPos.z;
                            break;
                        default:
                            throw new IllegalArgumentException("Unacceptable facingdir " + facing.getAxis()); //Impossible ?
                    }


                    double cubeDist = 1000;
                    for (BlockPos sectionPos : BlockPos.getAllInBox(new BlockPos(rendererPos.x + 0.5F - baseWidth / 2F, rendererPos.y + 0.5F - baseWidth / 2F, rendererPos.z + 0.5F - baseWidth / 2F), new BlockPos(rendererPos.x + 0.5F + baseWidth / 2F, rendererPos.y + 0.5F + baseWidth / 2F, rendererPos.z + 0.5F + baseWidth / 2F))) {
                        List<AxisAlignedBB> aabbList = Lists.newArrayList();
                        int counter = 0;
                        while (aabbList.isEmpty() && counter <= 100) {
                            world.getBlockState(sectionPos).addCollisionBoxToList(world, sectionPos, bounding, aabbList, null, false);
                            sectionPos = sectionPos.add(poleDirection.getFacing().getDirectionVec());
                            counter++;
                        }
                        if (!aabbList.isEmpty()) {
                            for (AxisAlignedBB aabb : aabbList) {
                                double hitPoint = poleDirection.apply(aabb) - 0.5F;
                                if(Math.abs(hitPoint - renderPosition) < Math.abs(cubeDist)) {
                                    cubeDist = hitPoint - renderPosition;
                                }
                            }
                        }
                    }
                    if (cubeDist == 1000) {
                        continue;
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-te.getPos().getX() + rendererPos.x, -te.getPos().getY() + rendererPos.y, -te.getPos().getZ() + rendererPos.z);

                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    GlStateManager.color(1f, 1f, 1f, 1f);
                    Tessellator tes = Tessellator.getInstance();
                    this.mc.renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/skeletal_builder.png"));//TODO: cache?


                    //Get the facing direction relative to the model direction.
                    EnumFacing rotP = poleFacing;

                    float rotationTimes = angle;

                    EnumFacing.Axis axis = rotVec.x == 1 ? EnumFacing.Axis.X : EnumFacing.Axis.Z;

                    while(rotationTimes > 0) {

                        rotP = rotP.rotateAround(axis);

                        rotationTimes -= 90F;
                    }


                    float rotation;

                    //When rotating the teRot, the axis used to go in a certian direction changes, therefore we should interpolate between the two angles.
                    if (poleFacing.getAxis() != facing.getAxis()) { //Interpolate between the 2 axis

                        EnumFacing from = rotP;
                        EnumFacing to = rotP.rotateY().getOpposite();

                        if (teRot >= 270) {
                            from = rotP.rotateY();
                            to = rotP;
                        } else if (teRot >= 180) {
                            from = rotP.getOpposite();
                            to = rotP.rotateY();
                        } else if (teRot >= 90) {
                            from = rotP.rotateY().getOpposite();
                            to = rotP.getOpposite();
                        }

                        float fromRot = this.getRotation(from, facing, cube, teRot);
                        float toRot = this.getRotation(to, facing, cube, teRot);

                        rotation = fromRot + (toRot - fromRot) * (teRot % 90F) / 90F;
                    } else {
                        rotation = this.getRotation(rotP, facing, cube, teRot);
                    }

                    switch (poleFacing.getAxis()) {
                        case X:
                            GlStateManager.rotate(poleFacing == EnumFacing.EAST ? 90F : 270F, 0, 0, 1);
                            break;
                        case Y:
                            GlStateManager.rotate(poleFacing == EnumFacing.UP ? 180F : 0F, 1, 0, 0);
                            break;
                        case Z:
                            GlStateManager.rotate(poleFacing == EnumFacing.NORTH ? 90F : 270F, 1, 0, 0);
                            break;
                    }
                    GlStateManager.rotate(rotation, 0, 1, 0);
                    GlStateManager.translate(-poleWidth / 2F, 0, -poleWidth / 2F);



                    double poleLength = cubeDist * -poleDirection.getFacing().getAxisDirection().getOffset();
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                    //Render pole
                    buff.pos(0, 0, poleWidth).tex(0, 0).normal(0, 0, 1).endVertex();
                    buff.pos(0, poleLength, poleWidth).tex(0, poleLength).normal(0, 0, 1).endVertex();
                    buff.pos(poleWidth, poleLength, poleWidth).tex(1 / textureWidth, poleLength).normal(0, 0, 1).endVertex();
                    buff.pos(poleWidth, 0, poleWidth).tex(1 / textureWidth, 0).normal(0, 0, 1).endVertex();

                    buff.pos(0, 0, 0).tex(0, 0).normal(-1, 0, 0).endVertex();
                    buff.pos(0, poleLength, 0).tex(0, poleLength).normal(-1, 0, 0).endVertex();
                    buff.pos(0, poleLength, poleWidth).tex(1 / textureWidth, poleLength).normal(-1, 0, 0).endVertex();
                    buff.pos(0, 0, poleWidth).tex(1 / textureWidth, 0).normal(-1, 0, -1).endVertex();

                    buff.pos(poleWidth, 0, 0).tex(0, 0).normal(0, 0, -1).endVertex();
                    buff.pos(poleWidth, poleLength, 0).tex(0, poleLength).normal(0, 0, -1).endVertex();
                    buff.pos(0, poleLength, 0).tex(1 / textureWidth, poleLength).normal(0, 0, -1).endVertex();
                    buff.pos(0, 0, 0).tex(1 / textureWidth, 0).normal(0, 0, -1).endVertex();

                    buff.pos(poleWidth, 0, poleWidth).tex(0, 0).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, poleLength, poleWidth).tex(0, poleLength).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, poleLength, 0).tex(1 / textureWidth, poleLength).normal(1, 0, 0).endVertex();
                    buff.pos(poleWidth, 0, 0).tex(1 / textureWidth, 0).normal(1, 0, 0).endVertex();

                    tes.draw();
                    GlStateManager.translate(poleWidth / 2F, poleLength, poleWidth / 2F);
                    this.renderCube(baseWidth, baseHeight, textureWidth, textureHeight, 1, 0);
                    GlStateManager.translate(0, baseHeight, 0);
                    this.renderCube(notchWidth, notchHeight, textureWidth, textureHeight, 1, 6);
                    GlStateManager.popMatrix();
                }
            }
        }
        GlStateManager.popMatrix();
    }

    private static void setVisability(ComponentAccess entity, TabulaModel tabulaModel) {
        Map<String, List<ModelRenderer>> modelChildMap = Maps.newHashMap();
        List<String> modelList = Lists.newArrayList();
        SkeletalBuilderComponent compoent = entity.getOrNull(ComponentHandler.SKELETAL_BUILDER);
        if(compoent != null) {
            for (String s : compoent.getIndividualBones()) {
                modelList.addAll(compoent.getBoneToModelMap().get(s));
            }
            int id = compoent.modelIndex % (modelList.size() + 1);
            List<ModelRenderer> nonHiddenCubes = Lists.newArrayList();
            if(id != 0) {
                String currentState = modelList.get(id - 1);
                List<String> activeStates = Lists.newArrayList();
                for (int i = 0; i < modelList.size(); i++) {
                    String model = modelList.get(i);
                    modelChildMap.put(model, MoreTabulaUtils.getAllChildren(tabulaModel.getCube(model), modelList));
                    if(i <= modelList.indexOf(currentState)) {
                        activeStates.add(model);
                    }
                }
                for (String activeState : activeStates) {
                    nonHiddenCubes.addAll(modelChildMap.get(activeState));
                }
                for (TabulaModelRenderer box : tabulaModel.getAllCubes()) {
                    if(nonHiddenCubes.contains(box)) {
                        box.setScaleX(1);
                        box.setScaleY(1);
                        box.setScaleZ(1);
                    } else {
//                        box.scaleX = 0;
//                        box.scaleY = 0;//TODO: Get rid of this?
//                        box.scaleZ = 0;
                    }
                }
            } else {
                for (TabulaModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
                    modelRenderer.setScaleX(1);
                    modelRenderer.setScaleY(1);
                    modelRenderer.setScaleZ(1);
                }
            }
        }
    }

    private static void resetVisability(TabulaModel tabulaModel) {
        for (TabulaModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
            modelRenderer.setScaleX(1);
            modelRenderer.setScaleY(1);
            modelRenderer.setScaleZ(1);
        }
    }

    private void renderCube(double width, double height, float textureWidth, float textureHeight, int uOffset, int vOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-width / 2F, 0, -width / 2F);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        //Chunks of code are in U-D-N-E-S-W order

        double uFrom = uOffset/textureWidth;
        double vFrom = vOffset/textureHeight;
        double uTo = uFrom+width*16F/textureWidth;
        double vTo = vFrom+width*16F/textureHeight;

        buff.pos(0, height, 0).tex(uFrom, vFrom).normal(0, 1, 0).endVertex();
        buff.pos(0, height, width).tex(uFrom, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, width).tex(uTo, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, 0).tex(uTo, vFrom).normal(0, 1, 0).endVertex();

        buff.pos(width, 0, 0).tex(uFrom, vFrom).normal(0, -1, 0).endVertex();
        buff.pos(width, 0, width).tex(uFrom, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, width).tex(uTo, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, 0).tex(uTo, vFrom).normal(0, -1, 0).endVertex();

        vTo = vFrom+height*16F/textureHeight;

        buff.pos(width, height, 0).tex(uFrom, vFrom).normal(0, 0, -1).endVertex();
        buff.pos(width, 0, 0).tex(uFrom, vTo).normal(0, 0, -1).endVertex();
        buff.pos(0, 0, 0).tex(uTo, vTo).normal(0, 0, -1).endVertex();
        buff.pos(0, height, 0).tex(uTo, vFrom).normal(0, 0, -1).endVertex();

        buff.pos(width, height, width).tex(uFrom, vFrom).normal(1, 0, 0).endVertex();
        buff.pos(width, 0, width).tex(uFrom, vTo).normal(1, 0, 0).endVertex();
        buff.pos(width, 0, 0).tex(uTo, vTo).normal(1, 0, 0).endVertex();
        buff.pos(width, height, 0).tex(uTo, vFrom).normal(1, 0, 0).endVertex();

        buff.pos(0, height, width).tex(uFrom, vFrom).normal(0, 0, 1).endVertex();
        buff.pos(0, 0, width).tex(uFrom, vTo).normal(0, 0, 1).endVertex();
        buff.pos(width, 0, width).tex(uTo, vTo).normal(0, 0, 1).endVertex();
        buff.pos(width, height, width).tex(uTo, vFrom).normal(0, 0, 1).endVertex();

        buff.pos(0, height, 0).tex(uFrom, vFrom).normal(-1, 0, 0).endVertex();
        buff.pos(0, 0, 0).tex(uFrom, vTo).normal(-1, 0, 0).endVertex();
        buff.pos(0, 0, width).tex(uTo, vTo).normal(-1, 0, 0).endVertex();
        buff.pos(0, height, width).tex(uTo, vFrom).normal(-1, 0, 0).endVertex();

        tes.draw();
        GlStateManager.popMatrix();
    }

    private float getRotation(EnumFacing poleFacing, EnumFacing teFacing, TabulaModelRenderer cube, float rot) {
        float rotation = 0;
        if(poleFacing.getAxis() == EnumFacing.Axis.Y) {
            rotation = rot;
        }
        TabulaModelRenderer reference = cube;
        while(reference != null) {
            double axisRotation;
            switch (poleFacing.getAxis()) {
                case X: axisRotation = reference.rotateAngleX; break;
                case Y: axisRotation = reference.rotateAngleY; break;
                case Z: axisRotation = reference.rotateAngleZ; break;
                default: throw new IllegalArgumentException("Unacceptable facingdir " + teFacing.getAxis());
            }
            rotation += Math.toDegrees(axisRotation);
            reference = reference.getParent();
        }
        return rotation * poleFacing.getAxisDirection().getOffset();
    }

    @Override
    public boolean isGlobalRenderer(SkeletalBuilderBlockEntity te) {
        return true;
    }
}
