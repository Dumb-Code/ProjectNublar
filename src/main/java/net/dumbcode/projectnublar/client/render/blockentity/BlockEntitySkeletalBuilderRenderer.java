package net.dumbcode.projectnublar.client.render.blockentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.RenderAdjustmentsComponent;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
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
import net.dumbcode.projectnublar.server.utils.DirectionUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class BlockEntitySkeletalBuilderRenderer extends TileEntityRenderer<SkeletalBuilderBlockEntity> {
    private Minecraft mc = Minecraft.getInstance();

    private static final float POLE_WIDTH = 1 / 16F;
    private static final float BASE_WIDTH = 6 / 16F;
    private static final float BASE_HEIGHT = 1 / 16F;

    private static final float NOTCH_WIDTH = 4 / 16F;
    private static final float NOTCH_HEIGHT = 2 / 16F;

    private static final float TEXTURE_WIDTH = 8F;
    private static final float TEXTURE_HEIGHT = 16F;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectNublar.MODID, "textures/entities/skeletal_builder.png");

    public BlockEntitySkeletalBuilderRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(SkeletalBuilderBlockEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light, int overlay) {
        Optional<DinosaurEntity> optionalDinosaurEntity = te.getDinosaurEntity();
        if(!optionalDinosaurEntity.isPresent()) {
            return;
        }
        BlockState state = te.getBlockState();
        if(state.getBlock() != BlockHandler.SKELETAL_BUILDER.get()) {
            return;
        }

        stack.pushPose();
        this.render(te, stack, light, buffers, partialTicks);
        stack.popPose();
    }

    @SneakyThrows
    private void render(SkeletalBuilderBlockEntity te, MatrixStack stack, int light, IRenderTypeBuffer buffers, float partialTicks) {
        Direction facing = te.getLevel().getBlockState(te.getBlockPos()).getValue(SkeletalBuilderBlock.FACING);
        float rawRotation = te.getSkeletalProperties().getRotation();
        float teRot = rawRotation + (te.getSkeletalProperties().getPrevRotation() - rawRotation) * partialTicks; //Interpolated version of rawRotation
        DinosaurEntity entity = te.getDinosaurEntity().orElseThrow(RuntimeException::new);//Impossible to throw
        Pair<Quaternion, Float> rotationDetails = this.createQuaternion(facing);
        DCMModel model = te.getModel();

        if(model == null) {
            return;
        }

        stack.translate(0.5, 0.5, 0.5);

        stack.pushPose();

        this.setupDinosaurGlState(rotationDetails.getLeft(), stack, teRot, entity);
        this.setBoxRotations(model, te.getPoseData());
        this.renderDinosaurModel(entity, model, stack, light, buffers.getBuffer(RenderType.entitySolid(te.getTexture())));
        this.resetDinosaurGlState();

        stack.popPose();

        this.renderAllPoles(buffers, stack, light, te, entity, facing, teRot, rotationDetails.getLeft(), rotationDetails.getRight(), facing.getAxis() == Direction.Axis.X);

    }

    private void setupDinosaurGlState(Quaternion rotation, MatrixStack stack, float teRot, ComponentAccess entity) {
        stack.mulPose(rotation);
        stack.mulPose(Vector3f.YP.rotationDegrees(teRot));
        stack.translate(0, -0.5F, 0);
        entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> stack.scale(floats[0],floats[1],floats[2]));
    }

    private void resetDinosaurGlState() {

    }

    private void setBoxRotations(DCMModel model, Map<String, TaxidermyHistory.CubeProps> poseData) {
        for(DCMModelRenderer box : model.getAllCubes()) {
            TaxidermyHistory.CubeProps cube = poseData.get(box.getName());
            if(cube != null) {
                cube.applyTo(box);
            } else {
                box.resetRotations();
            }
        }
    }

    private void renderDinosaurModel(ComponentAccess entity, DCMModel model, MatrixStack stack, int light, IVertexBuilder buff) {
        setVisibility(entity, model);
        model.renderBoxes(stack, light, buff);
        resetVisibility(model);
    }

    private Matrix4f createTranslationMatrix(BlockPos pos, Vector3i facingRot) {
        return Matrix4f.createTranslateMatrix(pos.getX() - facingRot.getX() / 2F, pos.getY() - facingRot.getY() / 2F, pos.getZ() - facingRot.getZ() / 2F);
    }

    private Matrix4f createRotationMatrix(float teRot) {
        Matrix4f rotateMatrix = new Matrix4f();
        rotateMatrix.setIdentity();
        rotateMatrix.multiply(Vector3f.YN.rotationDegrees(teRot));
        return rotateMatrix;
    }

    private Matrix4f createFacingMatrix(Quaternion rotationDetails) {
        Matrix4f facingMatrix = new Matrix4f();
        facingMatrix.setIdentity();
        facingMatrix.multiply(rotationDetails);
        return facingMatrix;
    }

    private void renderAllPoles(IRenderTypeBuffer buffers, MatrixStack stack, int light, SkeletalBuilderBlockEntity te, ComponentAccess entity, Direction facing, float teRot, Quaternion rotationDetails, float angle, boolean rotateOnX) {
        Matrix4f translateMatrix = this.createTranslationMatrix(te.getBlockPos(), facing.getNormal());
        Matrix4f rotateMatrix = this.createRotationMatrix(teRot);
        Matrix4f facingMatrix = this.createFacingMatrix(rotationDetails);

        Matrix4f translateFacingMatrix = new Matrix4f(translateMatrix);
        translateFacingMatrix.multiply(facingMatrix);

        Matrix4f teMatrix = new Matrix4f(translateFacingMatrix);
        teMatrix.multiply(rotateMatrix);

        //This:
        //outPoint = teMatrix.transform(point)

        //Is the same as:
        //point = translateMatrix.transform(point)
        //point = facingMatrix.transform(point)
        //outPoint = rotateMatrix.transform(point)

        for (SkeletalProperties.Pole pole : te.getSkeletalProperties().getPoles()) {
            this.renderPole(buffers, stack, light, pole, te, entity, facing, teRot, angle, rotateOnX, teMatrix);
        }
    }

    private void renderPole(IRenderTypeBuffer buffers, MatrixStack stack, int light, SkeletalProperties.Pole pole, SkeletalBuilderBlockEntity te, ComponentAccess entity, Direction facing, float teRot, float angle, boolean rotateOnX, Matrix4f teMatrix) {
        String anchoredPart = pole.getCubeName();
        PoleFacing poleDirection = pole.getFacing();
        Direction poleFacing = poleDirection.getFacing();
        DCMModelRenderer cube = te.getModel().getCube(anchoredPart);

        if (poleDirection != PoleFacing.NONE && cube != null) {
            Vector3f rendererPos = this.getRenderPosition(cube, entity, teMatrix);
            AxisAlignedBB boundingBox = this.createBoundingBox(poleFacing, rendererPos);
            float axisValue = this.getAxisValue(poleFacing, rendererPos);
            float cubeDist = this.getCubeDistance(te.getLevel(), poleDirection, boundingBox, rendererPos, axisValue);
            if (cubeDist == 1000) {
                return;
            }

            stack.pushPose();

            float poleLength = cubeDist * -poleDirection.getFacing().getAxisDirection().getStep();

            this.setupPoleGlState(stack, te.getBlockPos(), rendererPos, poleFacing, facing, teRot, angle, rotateOnX, cube);

            this.drawPole(stack, light, buffers, poleLength);
            this.drawPoleBase(stack, buffers, light, poleLength);
            this.drawPoleNotch(stack, buffers, light);

            stack.popPose();
        }
    }

    private void setupPoleGlState(MatrixStack stack, BlockPos pos, Vector3f rendererPos, Direction poleFacing, Direction facing, float teRot, float rotationAngle, boolean rotationOnX, DCMModelRenderer cube) {
        stack.translate(-pos.getX() + rendererPos.x(), -pos.getY() + rendererPos.y(), -pos.getZ() + rendererPos.z());

        switch (poleFacing.getAxis()) {
            case X:
                stack.mulPose(Vector3f.ZP.rotationDegrees(poleFacing == Direction.EAST ? 90F : 270F));
                break;
            case Y:
                stack.mulPose(Vector3f.XP.rotationDegrees(poleFacing == Direction.UP ? 180F : 0F));
                break;
            case Z:
                stack.mulPose(Vector3f.XP.rotationDegrees(poleFacing == Direction.NORTH ? 90F : 270F));
                break;
        }

        stack.mulPose(Vector3f.YP.rotationDegrees(this.getAbsolutePoleRotation(poleFacing, facing, rotationAngle, teRot, rotationOnX, cube)));
        stack.translate(-POLE_WIDTH / 2F, 0, -POLE_WIDTH / 2F);

    }

    private void drawPole(MatrixStack stack, int light, IRenderTypeBuffer buffers, float poleLength) {

        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();
        SimpleVertexBuilder buff = new SimpleVertexBuilder(buffers.getBuffer(RenderType.entitySolid(TEXTURE)), stack.last(), light);
        //Render pole

        buff.vertex(0, 0, POLE_WIDTH, 0, 0, 0, 0, 1);
        buff.vertex(0, poleLength, POLE_WIDTH, 0, poleLength, 0, 0, 1);
        buff.vertex(POLE_WIDTH, poleLength, POLE_WIDTH, 1 / TEXTURE_WIDTH, poleLength, 0, 0, 1);
        buff.vertex(POLE_WIDTH, 0, POLE_WIDTH, 1 / TEXTURE_WIDTH, 0, 0, 0, 1);

        buff.vertex(0, 0, 0, 0, 0, -1, 0, 0);
        buff.vertex(0, poleLength, 0, 0, poleLength, -1, 0, 0);
        buff.vertex(0, poleLength, POLE_WIDTH, 1 / TEXTURE_WIDTH, poleLength, -1, 0, 0);
        buff.vertex(0, 0, POLE_WIDTH, 1 / TEXTURE_WIDTH, 0, -1, 0, -1);

        buff.vertex(POLE_WIDTH, 0, 0, 0, 0, 0, 0, -1);
        buff.vertex(POLE_WIDTH, poleLength, 0, 0, poleLength, 0, 0, -1);
        buff.vertex(0, poleLength, 0, 1 / TEXTURE_WIDTH, poleLength, 0, 0, -1);
        buff.vertex(0, 0, 0, 1 / TEXTURE_WIDTH, 0, 0, 0, -1);

        buff.vertex(POLE_WIDTH, 0, POLE_WIDTH, 0, 0, 1, 0, 0);
        buff.vertex(POLE_WIDTH, poleLength, POLE_WIDTH, 0, poleLength, 1, 0, 0);
        buff.vertex(POLE_WIDTH, poleLength, 0, 1 / TEXTURE_WIDTH, poleLength, 1, 0, 0);
        buff.vertex(POLE_WIDTH, 0, 0, 1 / TEXTURE_WIDTH, 0, 1, 0, 0);
    }

    private void drawPoleBase(MatrixStack stack, IRenderTypeBuffer buffers, int light, double poleLength) {
        stack.translate(POLE_WIDTH / 2F, poleLength, POLE_WIDTH / 2F);
        this.renderCube(stack, buffers, light, BASE_WIDTH, BASE_HEIGHT, 1, 0);
    }

    private void drawPoleNotch(MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        stack.translate(0, BASE_HEIGHT, 0);
        this.renderCube(stack, buffers, light, NOTCH_WIDTH, NOTCH_HEIGHT, 1, 6);
    }

    private float getAbsolutePoleRotation(Direction poleFacing, Direction facing, float angle, float teRot, boolean rotationOnX, DCMModelRenderer cube) {
        //Get the facing direction relative to the model direction.
        Direction rotP = poleFacing;
        float rotationTimes = angle;
        Direction.Axis axis = rotationOnX ? Direction.Axis.X : Direction.Axis.Z;

        while(rotationTimes > 0) {
            rotP = DirectionUtils.rotateAround(rotP, axis);
            rotationTimes -= 90F;
        }

        //When rotating the teRot, the axis used to go in a certian direction changes, therefore we should interpolate between the two angles.
        if (poleFacing.getAxis() != facing.getAxis()) { //Interpolate between the 2 axis

            Direction from = rotP;
            Direction to = DirectionUtils.rotateY(rotP).getOpposite();

            if (teRot >= 270) {
                from = DirectionUtils.rotateY(rotP);
                to = rotP;
            } else if (teRot >= 180) {
                from = rotP.getOpposite();
                to = DirectionUtils.rotateY(rotP);
            } else if (teRot >= 90) {
                from = DirectionUtils.rotateY(rotP).getOpposite();
                to = rotP.getOpposite();
            }

            float fromRot = this.getRotation(from, facing, cube, teRot);
            float toRot = this.getRotation(to, facing, cube, teRot);

            return fromRot + (toRot - fromRot) * (teRot % 90F) / 90F;
        } else {
            return this.getRotation(rotP, facing, cube, teRot);
        }
    }

    private float getAxisValue(Direction poleFacing, Vector3f rendererPos) {
        switch (poleFacing.getAxis()) {
            case X: return rendererPos.x();
            case Y: return rendererPos.y();
            case Z: return rendererPos.z();
            default:
                throw new IllegalArgumentException("Unacceptable facingdir " + poleFacing.getAxis()); //Impossible ?
        }
    }

    private float getCubeDistance(World world, PoleFacing poleDirection, AxisAlignedBB bounding, Vector3f rendererPos, float axisValue) {
        float cubeDist = 1000;
        for (BlockPos sectionPos : BlockPos.betweenClosed(
            new BlockPos(rendererPos.x() + 0.5F - BASE_WIDTH / 2F, rendererPos.y() + 0.5F - BASE_WIDTH / 2F, rendererPos.z() + 0.5F - BASE_WIDTH / 2F),
            new BlockPos(rendererPos.x() + 0.5F + BASE_WIDTH / 2F, rendererPos.y() + 0.5F + BASE_WIDTH / 2F, rendererPos.z() + 0.5F + BASE_WIDTH / 2F)
        )) {
            List<AxisAlignedBB> aabbList = Lists.newArrayList();
            int counter = 0;
            while (aabbList.isEmpty() && counter <= 100) {
                //TODO: maybe we don't need to iterate over the aabbs.
                for (AxisAlignedBB aabb : world.getBlockState(sectionPos).getCollisionShape(world, sectionPos).toAabbs()) {
                    if(aabb.intersects(bounding)) {
                        aabbList.add(aabb);
                    }
                }


                sectionPos = sectionPos.mutable().move(poleDirection.getFacing().getNormal());
                counter++;
            }
            if (!aabbList.isEmpty()) {
                for (AxisAlignedBB aabb : aabbList) {
                    float hitPoint = poleDirection.apply(aabb) - 0.5F;
                    if(Math.abs(hitPoint - axisValue) < Math.abs(cubeDist)) {
                        cubeDist = hitPoint - axisValue;
                    }
                }
            }
        }
        return cubeDist;
    }

    private Vector3f getRenderPosition(DCMModelRenderer cube, ComponentAccess entity, Matrix4f teMatrix) {
        Vector3f partOrigin = DCMUtils.getModelPosAlpha(cube, 0.5F, 0.5F, 0.5F);
        partOrigin = new Vector3f(-partOrigin.x(), /*No need to minus the y, as we flip the model around anyway*/partOrigin.y(), -partOrigin.z());

        Vector3f rendererPos = new Vector3f(partOrigin.x(), partOrigin.y() + 1.5F, partOrigin.z());

        entity.get(EntityComponentTypes.RENDER_ADJUSTMENTS).map(RenderAdjustmentsComponent::getScale).ifPresent(floats -> rendererPos.mul(floats[0], floats[1], floats[2]));

        Vector4f toMul = new Vector4f(rendererPos);
        toMul.transform(teMatrix);

        return new Vector3f(toMul.x(), toMul.y(), toMul.z());
    }

    private AxisAlignedBB createBoundingBox(Direction poleFacing, Vector3f rendererPos) {
        return new AxisAlignedBB(0, 0, 0, poleFacing.getStepX() * 256F, poleFacing.getStepY() * 256F, poleFacing.getStepZ() * 256F)
                .move(rendererPos.x() + 0.5F, rendererPos.y() + 0.5F, rendererPos.z() + 0.5F)
                .inflate(BASE_WIDTH / 2F);
    }

    private Pair<Quaternion, Float> createQuaternion(Direction facing) {
        float angle;
        switch (facing.getAxis()) { //There gotta be a better way than this
            case X:
                angle = facing == Direction.WEST ? 90F : 270F;
                return Pair.of(Vector3f.ZP.rotationDegrees(angle), angle);
            case Y:
                angle = facing == Direction.UP ? 0F : 180F;
                return Pair.of(Vector3f.XP.rotationDegrees(angle), angle);
            case Z:
                angle = facing == Direction.SOUTH ? 90F : 270F;
                return Pair.of(Vector3f.XP.rotationDegrees(angle), angle);
            default:
                return Pair.of(Quaternion.ONE, 0F);
        }
    }

    private static void setVisibility(ComponentAccess entity, DCMModel tabulaModel) {
        List<String> modelList = Lists.newArrayList();
        SkeletalBuilderComponent compoent = entity.getOrNull(ComponentHandler.SKELETAL_BUILDER);
        if(compoent != null) {
            for (String s : compoent.getIndividualBones()) {
                modelList.addAll(compoent.getBoneToModelMap().get(s));
            }
            int id = compoent.modelIndex % (modelList.size() + 1);
            List<DCMModelRenderer> nonHiddenCubes = Lists.newArrayList();
            if(id != 0) {
                List<String> activeStates = Lists.newArrayList();
                Map<String, List<DCMModelRenderer>> modelChildMap = Maps.newHashMap();

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

    private static void resetScaleForAll(DCMModel tabulaModel, Predicate<DCMModelRenderer> predicate) {
        for (DCMModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
            modelRenderer.setHideButShowChildren(!predicate.test(modelRenderer));
        }
    }

    private static void generateModelChildMap(Map<String, List<DCMModelRenderer>> modelChildMap, List<String> activeStates, List<String> modelList, int id, DCMModel tabulaModel) {
        String currentState = modelList.get(id - 1);
        for (int i = 0; i < modelList.size(); i++) {
            String model = modelList.get(i);
            modelChildMap.put(model, MoreTabulaUtils.getAllChildren(tabulaModel.getCube(model), modelList));
            if(i <= modelList.indexOf(currentState)) {
                activeStates.add(model);
            }
        }
    }

    private static void resetVisibility(DCMModel tabulaModel) {
        for (DCMModelRenderer modelRenderer : tabulaModel.getAllCubes()) {
            modelRenderer.setHideButShowChildren(false);
        }
    }

    private void renderCube(MatrixStack stack, IRenderTypeBuffer buffer, int light, float width, float height, int uOffset, int vOffset) {
        stack.pushPose();
        stack.translate(-width / 2F, 0, -width / 2F);

        //Chunks of code are in U-D-N-E-S-W order

        float uFrom = uOffset/TEXTURE_WIDTH;
        float vFrom = vOffset/TEXTURE_HEIGHT;
        float uTo = uFrom+width*16F/TEXTURE_WIDTH;
        float vTo = vFrom+width*16F/TEXTURE_HEIGHT;

        SimpleVertexBuilder buff = new SimpleVertexBuilder(buffer.getBuffer(RenderType.entitySolid(TEXTURE)), stack.last(), light);

        buff.vertex(0, height, 0, uFrom, vFrom, 0, 1, 0);
        buff.vertex(0, height, width, uFrom, vTo, 0, 1, 0);
        buff.vertex(width, height, width, uTo, vTo, 0, 1, 0);
        buff.vertex(width, height, 0, uTo, vFrom, 0, 1, 0);

        buff.vertex(width, 0, 0, uFrom, vFrom, 0, -1, 0);
        buff.vertex(width, 0, width, uFrom, vTo, 0, -1, 0);
        buff.vertex(0, 0, width, uTo, vTo, 0, -1, 0);
        buff.vertex(0, 0, 0, uTo, vFrom, 0, -1, 0);

        vTo = vFrom+height*16F/TEXTURE_HEIGHT;

        buff.vertex(width, height, 0, uFrom, vFrom, 0, 0, -1);
        buff.vertex(width, 0, 0, uFrom, vTo, 0, 0, -1);
        buff.vertex(0, 0, 0, uTo, vTo, 0, 0, -1);
        buff.vertex(0, height, 0, uTo, vFrom, 0, 0, -1);

        buff.vertex(width, height, width, uFrom, vFrom, 1, 0, 0);
        buff.vertex(width, 0, width, uFrom, vTo, 1, 0, 0);
        buff.vertex(width, 0, 0, uTo, vTo, 1, 0, 0);
        buff.vertex(width, height, 0, uTo, vFrom, 1, 0, 0);

        buff.vertex(0, height, width, uFrom, vFrom, 0, 0, 1);
        buff.vertex(0, 0, width, uFrom, vTo, 0, 0, 1);
        buff.vertex(width, 0, width, uTo, vTo, 0, 0, 1);
        buff.vertex(width, height, width, uTo, vFrom, 0, 0, 1);

        buff.vertex(0, height, 0, uFrom, vFrom, -1, 0, 0);
        buff.vertex(0, 0, 0, uFrom, vTo, -1, 0, 0);
        buff.vertex(0, 0, width, uTo, vTo, -1, 0, 0);
        buff.vertex(0, height, width, uTo, vFrom, -1, 0, 0);

        stack.popPose();
    }



    private float getRotation(Direction poleFacing, Direction teFacing, DCMModelRenderer cube, float rot) {
        float rotation = 0;
        if(poleFacing.getAxis() == Direction.Axis.Y) {
            rotation = -rot;
        }
        DCMModelRenderer reference = cube;
        while(reference != null) {
            double axisRotation;
            switch (poleFacing.getAxis()) {
                case X: axisRotation = reference.xRot; break;
                case Y: axisRotation = reference.yRot; break;
                case Z: axisRotation = reference.zRot; break;
                default: throw new IllegalArgumentException("Unacceptable facingdir " + teFacing.getAxis());
            }
            rotation += Math.toDegrees(axisRotation);
            reference = reference.getParent();
        }
        return rotation * poleFacing.getAxisDirection().getStep();
    }


    @Override
    public boolean shouldRenderOffScreen(SkeletalBuilderBlockEntity p_188185_1_) {
        return true;
    }

    @RequiredArgsConstructor
    private static class SimpleVertexBuilder {
        private final IVertexBuilder buff;
        private final MatrixStack.Entry entry;
        private final int light;

        public void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
            this.buff.vertex(this.entry.pose(), x, y, z)
                .color(1F, 1F, 1F, 1F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(this.light)
                .normal(this.entry.normal(), nx, ny, nz)
                .endVertex();
        }
    }
}
