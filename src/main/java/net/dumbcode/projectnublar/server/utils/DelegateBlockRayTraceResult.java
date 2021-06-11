package net.dumbcode.projectnublar.server.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class DelegateBlockRayTraceResult extends BlockRayTraceResult {
    private final BlockRayTraceResult delegate;

    public static DelegateBlockRayTraceResult of(BlockRayTraceResult delegate) {
        return delegate == null ? null : new DelegateBlockRayTraceResult(delegate);
    }

    private DelegateBlockRayTraceResult(BlockRayTraceResult delegate) {
        super(delegate.getLocation(), delegate.getDirection(), delegate.getBlockPos(), delegate.isInside());
        this.delegate = delegate;
        this.hitInfo = delegate.hitInfo;
    }

    public BlockRayTraceResult withDirection(Direction p_216351_1_) {
        return DelegateBlockRayTraceResult.of(this.delegate.withDirection(p_216351_1_));
    }

    public BlockRayTraceResult withPosition(BlockPos p_237485_1_) {
        return DelegateBlockRayTraceResult.of(this.delegate.withPosition(p_237485_1_));
    }

    public BlockPos getBlockPos() {
        return this.delegate.getBlockPos();
    }

    public Direction getDirection() {
        return this.delegate.getDirection();
    }

    public Type getType() {
        return this.delegate.getType();
    }

    public boolean isInside() {
        return this.delegate.isInside();
    }

    public double distanceTo(Entity p_237486_1_) {
        return this.delegate.distanceTo(p_237486_1_);
    }

    public Vector3d getLocation() {
        return this.delegate.getLocation();
    }
}
