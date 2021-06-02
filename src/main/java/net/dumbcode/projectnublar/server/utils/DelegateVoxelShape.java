package net.dumbcode.projectnublar.server.utils;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.Supplier;

public class DelegateVoxelShape extends VoxelShape {
    private final VoxelShape delegate;
    private final Callback callback;

    public DelegateVoxelShape(VoxelShape delegate, Callback callback) {
        super(delegate.shape);
        this.delegate = delegate;
        this.callback = callback;
    }

    public BlockRayTraceResult clip(Vector3d from, Vector3d to, BlockPos offset) {
        return this.callback.getRaytrace(from, to, offset, () -> this.delegate.clip(from, to, offset));
    }

    @Override
    public DoubleList getCoords(Direction.Axis axis) {
        return this.delegate.getCoords(axis);
    }

    public interface Callback {
        BlockRayTraceResult getRaytrace(Vector3d from, Vector3d to, BlockPos offset, Supplier<BlockRayTraceResult> fallback);
    }
}
