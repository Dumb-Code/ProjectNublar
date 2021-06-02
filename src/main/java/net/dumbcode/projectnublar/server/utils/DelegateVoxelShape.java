package net.dumbcode.projectnublar.server.utils;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class DelegateVoxelShape extends VoxelShape {
    private final VoxelShape shape;

    public DelegateVoxelShape(VoxelShape shape) {
        super(null);
        this.shape = shape;
    }

    public BlockRayTraceResult clip(Vector3d p_212433_1_, Vector3d p_212433_2_, BlockPos p_212433_3_) {
        BlockRayTraceResult result = this.shape.clip(p_212433_1_, p_212433_2_, p_212433_3_);
    }

    public double min(Direction.Axis p_197762_1_) {
        return this.shape.min(p_197762_1_);
    }

    public double max(Direction.Axis p_197758_1_) {
        return this.shape.max(p_197758_1_);
    }

    public AxisAlignedBB bounds() {
        return this.shape.bounds();
    }

    public boolean isEmpty() {
        return this.shape.isEmpty();
    }

    public VoxelShape move(double p_197751_1_, double p_197751_3_, double p_197751_5_) {
        return this.shape.move(p_197751_1_, p_197751_3_, p_197751_5_);
    }

    public VoxelShape optimize() {
        return this.shape.optimize();
    }

    public void forAllEdges(VoxelShapes.ILineConsumer p_197754_1_) {
        this.shape.forAllEdges(p_197754_1_);
    }

    public void forAllBoxes(VoxelShapes.ILineConsumer p_197755_1_) {
        this.shape.forAllBoxes(p_197755_1_);
    }

    public List<AxisAlignedBB> toAabbs() {
        return this.shape.toAabbs();
    }

    public double max(Direction.Axis p_197760_1_, double p_197760_2_, double p_197760_4_) {
        return this.shape.max(p_197760_1_, p_197760_2_, p_197760_4_);
    }

    public VoxelShape getFaceShape(Direction p_212434_1_) {
        return this.shape.getFaceShape(p_212434_1_);
    }

    public double collide(Direction.Axis p_212430_1_, AxisAlignedBB p_212430_2_, double p_212430_3_) {
        return this.shape.collide(p_212430_1_, p_212430_2_, p_212430_3_);
    }

    @Override
    public DoubleList getCoords(Direction.Axis p_197757_1_) {
        return this.shape.getCoords(p_197757_1_);
    }
}
