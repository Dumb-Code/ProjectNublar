package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Math.PI;

public class BlockEntitySkeletalBuilderRenderer extends TileEntitySpecialRenderer<SkeletalBuilderBlockEntity> {
    private Minecraft mc = Minecraft.getMinecraft();

    private static final double POLE_WIDTH = 1 / 16F;
    private static final double BASE_WIDTH = 6 / 16F;
    private static final double BASE_HEIGHT = 1 / 16F;

    private static final double NOTCH_WIDTH = 4 / 16F;
    private static final double NOTCH_HEIGHT = 2 / 16F;

    private static final float TEXTURE_WIDTH = 8F;
    private static final float TEXTURE_HEIGHT = 16F;

    @Override
    public void render(SkeletalBuilderBlockEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Optional<DinosaurEntity> optionalDinosaurEntity = te.getDinosaurEntity();
        if(!optionalDinosaurEntity.isPresent()) {
            return;
        }
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if(state.getBlock() != BlockHandler.SKELETAL_BUILDER) { //Can sometimes happen when loading in a save. Not sure why, but it happens
            return;
        }

        GlStateManager.pushMatrix();
        this.render(te, x, y, z, partialTicks);
        GlStateManager.popMatrix();
    }

    @SneakyThrows
    private void render(SkeletalBuilderBlockEntity te, double x, double y, double z, float partialTicks) {

        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(SkeletalBuilderBlock.FACING);
        float rawRotation = te.getSkeletalProperties().getRotation();
        float teRot = rawRotation + (te.getSkeletalProperties().getPrevRotation() - rawRotation) * partialTicks; //Interpolated version of rawRotation
        DinosaurEntity entity = te.getDinosaurEntity().orElseThrow(RuntimeException::new);//Impossible to throw
        Pair<Vector3f, Float> rotationDetails = this.createRotationDetails(facing);
        TabulaModel model = te.getModel();

        if(model == null) {
            return;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        GlStateManager.pushMatrix();

        this.setupDinosaurGlState(rotationDetails, teRot, entity, te.getTexture());
        this.setBoxRotations(model, te.getPoseData());
        this.renderDinosaurModel(entity, model);
        this.resetDinosaurGlState();

        GlStateManager.popMatrix();

        this.renderAllPoles(te, entity, facing, teRot, rotationDetails);

    }

    private void setupDinosaurGlState(Pair<Vector3f, Float> rotationDetails, float teRot, ComponentAccess entity, ResourceLocation texture) {
        GlStateManager.enableAlpha();
        Vector3f axis = rotationDetails.getLeft();
        GlStateManager.rotate(rotationDetails.getRight(), axis.x, axis.y, axis.z);
        GlStateManager.rotate(teRot, 0, 1, 0);
        GlStateManager.translate(0, -0.5F, 0);
        entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> GlStateManager.scale(floats[0],floats[1],floats[2]));
        GlStateManager.translate(0f, 1.5f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        GlStateManager.disableCull();

        mc.getTextureManager().bindTexture(texture);
    }

    private void resetDinosaurGlState() {
        GlStateManager.enableCull();
    }

    private void setBoxRotations(TabulaModel model, Map<String, Vector3f> poseData) {
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
    }

    private void renderDinosaurModel(ComponentAccess entity, TabulaModel model) {
        setVisability(entity, model);
        model.renderBoxes(1F/16F);
        resetVisability(model);
    }

    private Matrix4d createTranslationMatrix(BlockPos pos, Vec3i facingRot) {
        Matrix4d translateMatrix = new Matrix4d();
        translateMatrix.set(new Vector3d(pos.getX() - facingRot.getX() / 2F, pos.getY() - facingRot.getY() / 2F, pos.getZ() - facingRot.getZ() / 2F));
        return translateMatrix;
    }

    private Matrix4d createRotationMatrix(float teRot) {
        Matrix4d rotateMatrix = new Matrix4d();
        rotateMatrix.rotY(teRot / 180D * PI);
        return rotateMatrix;
    }

    private Matrix4d createFacingMatrix(Pair<Vector3f, Float> rotationDetails) {
        Matrix4d facingMatrix = new Matrix4d();
        Vector3f rotVec = rotationDetails.getLeft();
        float angle = rotationDetails.getRight();

        if(rotVec.x != 0) {
            facingMatrix.rotX(angle / 180F * PI);
        } else if(rotVec.y != 0) {
            facingMatrix.rotY(angle / 180F * PI);
        } else if(rotVec.z != 0) {
            facingMatrix.rotZ(angle / 180F * PI);
        }
        return facingMatrix;
    }

    private void renderAllPoles(SkeletalBuilderBlockEntity te, ComponentAccess entity, EnumFacing facing, float teRot, Pair<Vector3f, Float> rotationDetails) {
        Matrix4d translateMatrix = this.createTranslationMatrix(te.getPos(), facing.getDirectionVec());
        Matrix4d rotateMatrix = this.createRotationMatrix(teRot);
        Matrix4d facingMatrix = this.createFacingMatrix(rotationDetails);

        Matrix4d translateFacingMatrix = new Matrix4d();
        translateFacingMatrix.mul(translateMatrix, facingMatrix);

        Matrix4d teMatrix = new Matrix4d();
        teMatrix.mul(translateFacingMatrix, rotateMatrix);

        //This:
        //outPoint = teMatrix.transform(point)

        //Is the same as:
        //point = translateMatrix.transform(point)
        //point = facingMatrix.transform(point)
        //outPoint = rotateMatrix.transform(point)

        for (SkeletalProperties.Pole pole : te.getSkeletalProperties().getPoles()) {
            this.renderPole(pole, te, entity, facing, teRot, rotationDetails, teMatrix);
        }
    }

    private void renderPole(SkeletalProperties.Pole pole, SkeletalBuilderBlockEntity te, ComponentAccess entity, EnumFacing facing, float teRot, Pair<Vector3f, Float> rotationDetails, Matrix4d teMatrix) {
        String anchoredPart = pole.getCubeName();
        PoleFacing poleDirection = pole.getFacing();
        EnumFacing poleFacing = poleDirection.getFacing();
        TabulaModelRenderer cube = te.getModel().getCube(anchoredPart);

        if (poleDirection != PoleFacing.NONE && cube != null && (cube.getScaleX() != 0 || cube.getScaleY() != 0 || cube.getScaleZ() != 0)) {

            Point3d rendererPos = this.getRenderPosition(cube, entity, teMatrix);
            AxisAlignedBB boundingBox = this.createBoundingBox(poleFacing, rendererPos);
            double axisValue = this.getAxisValue(poleFacing, rendererPos);
            double cubeDist = this.getCubeDistance(this.getWorld(), poleDirection, boundingBox, rendererPos, axisValue);
            if (cubeDist == 1000) {
                return;
            }

            GlStateManager.pushMatrix();

            double poleLength = cubeDist * -poleDirection.getFacing().getAxisDirection().getOffset();

            this.setupPoleGlState(te.getPos(), rendererPos, poleFacing, facing, teRot, rotationDetails, cube);

            this.drawPole(poleLength);
            this.drawPoleBase(poleLength);
            this.drawPoleNotch();

            GlStateManager.popMatrix();
        }
    }

    private void setupPoleGlState(BlockPos pos, Point3d rendererPos, EnumFacing poleFacing, EnumFacing facing, float teRot, Pair<Vector3f, Float> rotationDetails, TabulaModelRenderer cube) {
        GlStateManager.translate(-pos.getX() + rendererPos.x, -pos.getY() + rendererPos.y, -pos.getZ() + rendererPos.z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

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

        GlStateManager.rotate(this.getAbsolutePoleRotation(poleFacing, facing, rotationDetails.getRight(), teRot, rotationDetails.getLeft(), cube), 0, 1, 0);
        GlStateManager.translate(-POLE_WIDTH / 2F, 0, -POLE_WIDTH / 2F);

        this.mc.renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/skeletal_builder.png"));//TODO: cache?
    }

    private void drawPole(double poleLength) {

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buff = tess.getBuffer();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        //Render pole
        buff.pos(0, 0, POLE_WIDTH).tex(0, 0).normal(0, 0, 1).endVertex();
        buff.pos(0, poleLength, POLE_WIDTH).tex(0, poleLength).normal(0, 0, 1).endVertex();
        buff.pos(POLE_WIDTH, poleLength, POLE_WIDTH).tex(1 / TEXTURE_WIDTH, poleLength).normal(0, 0, 1).endVertex();
        buff.pos(POLE_WIDTH, 0, POLE_WIDTH).tex(1 / TEXTURE_WIDTH, 0).normal(0, 0, 1).endVertex();

        buff.pos(0, 0, 0).tex(0, 0).normal(-1, 0, 0).endVertex();
        buff.pos(0, poleLength, 0).tex(0, poleLength).normal(-1, 0, 0).endVertex();
        buff.pos(0, poleLength, POLE_WIDTH).tex(1 / TEXTURE_WIDTH, poleLength).normal(-1, 0, 0).endVertex();
        buff.pos(0, 0, POLE_WIDTH).tex(1 / TEXTURE_WIDTH, 0).normal(-1, 0, -1).endVertex();

        buff.pos(POLE_WIDTH, 0, 0).tex(0, 0).normal(0, 0, -1).endVertex();
        buff.pos(POLE_WIDTH, poleLength, 0).tex(0, poleLength).normal(0, 0, -1).endVertex();
        buff.pos(0, poleLength, 0).tex(1 / TEXTURE_WIDTH, poleLength).normal(0, 0, -1).endVertex();
        buff.pos(0, 0, 0).tex(1 / TEXTURE_WIDTH, 0).normal(0, 0, -1).endVertex();

        buff.pos(POLE_WIDTH, 0, POLE_WIDTH).tex(0, 0).normal(1, 0, 0).endVertex();
        buff.pos(POLE_WIDTH, poleLength, POLE_WIDTH).tex(0, poleLength).normal(1, 0, 0).endVertex();
        buff.pos(POLE_WIDTH, poleLength, 0).tex(1 / TEXTURE_WIDTH, poleLength).normal(1, 0, 0).endVertex();
        buff.pos(POLE_WIDTH, 0, 0).tex(1 / TEXTURE_WIDTH, 0).normal(1, 0, 0).endVertex();

        tess.draw();
    }

    private void drawPoleBase(double poleLength) {
        GlStateManager.translate(POLE_WIDTH / 2F, poleLength, POLE_WIDTH / 2F);
        this.renderCube(BASE_WIDTH, BASE_HEIGHT, 1, 0);
    }

    private void drawPoleNotch() {
        GlStateManager.translate(0, BASE_HEIGHT, 0);
        this.renderCube(NOTCH_WIDTH, NOTCH_HEIGHT, 1, 6);
    }

    private float getAbsolutePoleRotation(EnumFacing poleFacing, EnumFacing facing, float angle, float teRot, Vector3f rotVec, TabulaModelRenderer cube) {
        //Get the facing direction relative to the model direction.
        EnumFacing rotP = poleFacing;
        float rotationTimes = angle;
        EnumFacing.Axis axis = rotVec.x == 1 ? EnumFacing.Axis.X : EnumFacing.Axis.Z;

        while(rotationTimes > 0) {
            rotP = rotP.rotateAround(axis);
            rotationTimes -= 90F;
        }

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

            return fromRot + (toRot - fromRot) * (teRot % 90F) / 90F;
        } else {
            return this.getRotation(rotP, facing, cube, teRot);
        }
    }

    private double getAxisValue(EnumFacing poleFacing, Point3d rendererPos) {
        switch (poleFacing.getAxis()) {
            case X: return rendererPos.x;
            case Y: return rendererPos.y;
            case Z: return rendererPos.z;
            default:
                throw new IllegalArgumentException("Unacceptable facingdir " + poleFacing.getAxis()); //Impossible ?
        }
    }

    private double getCubeDistance(World world, PoleFacing poleDirection, AxisAlignedBB bounding, Point3d rendererPos, double axisValue) {
        double cubeDist = 1000;
        for (BlockPos sectionPos : BlockPos.getAllInBox(new BlockPos(rendererPos.x + 0.5F - BASE_WIDTH / 2F, rendererPos.y + 0.5F - BASE_WIDTH / 2F, rendererPos.z + 0.5F - BASE_WIDTH / 2F), new BlockPos(rendererPos.x + 0.5F + BASE_WIDTH / 2F, rendererPos.y + 0.5F + BASE_WIDTH / 2F, rendererPos.z + 0.5F + BASE_WIDTH / 2F))) {
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
                    if(Math.abs(hitPoint - axisValue) < Math.abs(cubeDist)) {
                        cubeDist = hitPoint - axisValue;
                    }
                }
            }
        }
        return cubeDist;
    }

    private Point3d getRenderPosition(TabulaModelRenderer cube, ComponentAccess entity, Matrix4d teMatrix) {
        Vec3d partOrigin = TabulaUtils.getModelPosAlpha(cube, 0.5F, 0.5F, 0.5F);
        partOrigin = new Vec3d(-partOrigin.x, /*No need to minus the y, as we flip the model around anyway*/partOrigin.y, -partOrigin.z);

        Point3d rendererPos = new Point3d(partOrigin.x, partOrigin.y + 1.5, partOrigin.z);

        entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> {
            rendererPos.x *= floats[0];
            rendererPos.y *= floats[1];
            rendererPos.z *= floats[2];

        });

        teMatrix.transform(rendererPos);

        return rendererPos;
    }

    private AxisAlignedBB createBoundingBox(EnumFacing poleFacing, Point3d rendererPos) {
        return new AxisAlignedBB(0, 0, 0, poleFacing.getXOffset() * 256F, poleFacing.getYOffset() * 256F, poleFacing.getZOffset() * 256F)
                .offset(rendererPos.x + 0.5F, rendererPos.y + 0.5F, rendererPos.z + 0.5F)
                .grow(BASE_WIDTH / 2F);
    }

    private Pair<Vector3f, Float> createRotationDetails(EnumFacing facing) {
        switch (facing.getAxis()) { //There gotta be a better way than this
            case X:
                return Pair.of(new Vector3f(0, 0, 1), facing == EnumFacing.WEST ? 90F : 270F);
            case Y:
                return Pair.of(new Vector3f(1, 0, 0), facing == EnumFacing.UP ? 0F : 180F);
            case Z:
                return Pair.of(new Vector3f(1, 0, 0), facing == EnumFacing.SOUTH ? 90F : 270F);
            default:
                return Pair.of(new Vector3f(), 90F);
        }
    }

    private static void setVisability(ComponentAccess entity, TabulaModel tabulaModel) {
        List<String> modelList = Lists.newArrayList();
        SkeletalBuilderComponent compoent = entity.getOrNull(ComponentHandler.SKELETAL_BUILDER);
        if(compoent != null) {
            for (String s : compoent.getIndividualBones()) {
                modelList.addAll(compoent.getBoneToModelMap().get(s));
            }
            int id = compoent.modelIndex % (modelList.size() + 1);
            List<ModelRenderer> nonHiddenCubes = Lists.newArrayList();
            if(id != 0) {
                List<String> activeStates = Lists.newArrayList();
                Map<String, List<ModelRenderer>> modelChildMap = Maps.newHashMap();

                generateModelChildMap(modelChildMap, activeStates, modelList, id, tabulaModel);

                for (String activeState : activeStates) {
                    nonHiddenCubes.addAll(modelChildMap.get(activeState));
                }
                resetScaleForAll(tabulaModel, nonHiddenCubes::contains);

            } else {
                resetScaleForAll(tabulaModel, tmr -> true);
            }
        }
    }

    private static void resetScaleForAll(TabulaModel tabulaModel, Predicate<TabulaModelRenderer> predicate) {
        for (TabulaModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
            modelRenderer.setHideButShowChildren(!predicate.test(modelRenderer));
        }
    }

    private static void generateModelChildMap(Map<String, List<ModelRenderer>> modelChildMap, List<String> activeStates, List<String> modelList, int id, TabulaModel tabulaModel) {
        String currentState = modelList.get(id - 1);
        for (int i = 0; i < modelList.size(); i++) {
            String model = modelList.get(i);
            modelChildMap.put(model, MoreTabulaUtils.getAllChildren(tabulaModel.getCube(model), modelList));
            if(i <= modelList.indexOf(currentState)) {
                activeStates.add(model);
            }
        }
    }

    private static void resetVisability(TabulaModel tabulaModel) {
        for (TabulaModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
            modelRenderer.setHideButShowChildren(false);
        }
    }

    private void renderCube(double width, double height, int uOffset, int vOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-width / 2F, 0, -width / 2F);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        //Chunks of code are in U-D-N-E-S-W order

        double uFrom = uOffset/TEXTURE_WIDTH;
        double vFrom = vOffset/TEXTURE_HEIGHT;
        double uTo = uFrom+width*16F/TEXTURE_WIDTH;
        double vTo = vFrom+width*16F/TEXTURE_HEIGHT;

        buff.pos(0, height, 0).tex(uFrom, vFrom).normal(0, 1, 0).endVertex();
        buff.pos(0, height, width).tex(uFrom, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, width).tex(uTo, vTo).normal(0, 1, 0).endVertex();
        buff.pos(width, height, 0).tex(uTo, vFrom).normal(0, 1, 0).endVertex();

        buff.pos(width, 0, 0).tex(uFrom, vFrom).normal(0, -1, 0).endVertex();
        buff.pos(width, 0, width).tex(uFrom, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, width).tex(uTo, vTo).normal(0, -1, 0).endVertex();
        buff.pos(0, 0, 0).tex(uTo, vFrom).normal(0, -1, 0).endVertex();

        vTo = vFrom+height*16F/TEXTURE_HEIGHT;

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
            rotation = -rot;
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
