package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.vector.*;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.Collections;
import java.util.List;

@Getter
public class RotatedRayBox {

    private final AxisAlignedBB box;
    private final Vector3f origin;
    private final Matrix4f forward;
    private final Matrix4f backwards;

    public RotatedRayBox(AxisAlignedBB box, Vector3f origin, Matrix4f forward, Matrix4f backwards) {
        this.box = box;
        this.origin = origin;
        this.forward = forward;
        this.backwards = backwards;
    }

    @Nullable
    public Result rayTrace(IPosition startIn, IPosition endIn) {
        Vector3f start = new Vector3f((float)(startIn.x() - this.origin.x()), (float)(startIn.y() - this.origin.y()), (float)(startIn.z() - this.origin.z()));
        Vector3f end = new Vector3f((float)(endIn.x() - this.origin.x()), (float)(endIn.y() - this.origin.y()), (float)(endIn.z() - this.origin.z()));

        this.transform(start, this.forward);
        this.transform(end, this.forward);

        Vector3d sv = new Vector3d(start);
        Vector3d ev = new Vector3d(end);

        Vector3d diff = sv.subtract(ev);

        //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
        sv = sv.add(diff.x*100, diff.y*100, diff.z*100);
        ev = ev.subtract(diff.x*100, diff.y*100, diff.z*100);

        BlockRayTraceResult result = AxisAlignedBB.clip(Collections.singleton(this.box), sv, ev, BlockPos.ZERO);
        if(result != null) {
            Direction hitDir = result.getDirection();
            Vector3d hit = result.getLocation();
            double dist = hit.distanceToSqr(start.x(), start.y(), start.z());

            Vector3f hitVec = new Vector3f(hit);
            this.transform(hitVec, this.backwards);


            Vector3i vec = result.getDirection().getNormal();
            Vector3f sidevec = new Vector3f(vec.getX(), vec.getY(), vec.getZ());
            this.transform(sidevec, this.backwards);


            result = new BlockRayTraceResult(new Vector3d(hitVec), Direction.getNearest(sidevec.x(), sidevec.y(), sidevec.z()), BlockPos.ZERO, true);
            result.hitInfo = dist;

            return new Result(this, result, hitDir, start, end, startIn, endIn, hit, dist);
        }
        return null;
    }

    private void transform(Vector3f vec, Matrix4f mat) {
        Vector4f v = new Vector4f(vec);
        v.transform(mat);
        vec.set(v.x(), v.y(), v.z());
    }

    public Vector3f[] points() {
        return points(RotatedRayBox.this.box, 0, 0, 0);
    }

    public Vector3f[] points(AxisAlignedBB box, double x, double y, double z) {
        int[] values = new int[] {0, 1};
        Vector3f[] points = new Vector3f[8];

        for (int xb : values) {
            for (int yb : values) {
                for (int zb : values) {
                    points[(xb << 2) + (yb << 1) + zb] = new Vector3f(
                        new Vector3d(
                            xb == 1 ? box.maxX : box.minX,
                            yb == 1 ? box.maxY : box.minY,
                            zb == 1 ? box.maxZ : box.minZ
                        ).add(x, y, z)
                    );
                }
            }
        }
        for (Vector3f point : points) {
            RotatedRayBox.this.transform(point, RotatedRayBox.this.backwards);
        }
        return points;
    }

    public static class Builder {
        private final AxisAlignedBB box;
        private Vec3d origin = Vec3d.ZERO;
        private Matrix4d matrix = new Matrix4d();

        private List<AxisAngle4d> backwards = Lists.newLinkedList();

        public Builder(AxisAlignedBB box) {
            this.box = box;
            this.matrix.setIdentity();
        }

        public Builder origin(double x, double y, double z) {
            this.origin = new Vec3d(x, y, z);
            return this;
        }

        public Builder rotate(double angle, double x, double y, double z) {
            Matrix4d diff = new Matrix4d();
            diff.setIdentity();
            diff.setRotation(new AxisAngle4d(x, y, z, angle));
            this.backwards.add(new AxisAngle4d(x, y, z, -angle));
            this.matrix.mul(diff);
            return this;
        }

        public RotatedRayBox build() {
            Matrix4d backwards = new Matrix4d();
            backwards.setIdentity();
            for (int i = this.backwards.size() - 1; i >= 0; i--) {
                Matrix4d diff = new Matrix4d();
                diff.setIdentity();
                diff.setRotation(this.backwards.get(i));
                backwards.mul(diff);
            }
            return new RotatedRayBox(this.box, this.origin, this.matrix, backwards);
        }
    }

    @Value
    public class Result {
        private final RotatedRayBox parent;
        private final BlockRayTraceResult result;
        private final Direction hitDir;
        private final Vector3d startRotated;
        private final Vector3d endRotated;
        private final Vector3d start;
        private final Vector3d end;
        private final Vector3d hitRotated;
        private final double distance;

        public void debugRender(MatrixStack stack, IRenderTypeBuffer buffers, double x, double y, double z) {
            stack.translate(x + this.parent.origin.x(), y + this.parent.origin.y(), z + this.parent.origin.z());
            Matrix4f pose = stack.last().pose();


            Vector3d sv = new Vector3d(this.startRotated.x, this.startRotated.y, this.startRotated.z);
            Vector3d ev = new Vector3d(this.endRotated.x, this.endRotated.y, this.endRotated.z);

            Vector3d diff = sv.subtract(ev);

            //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
            sv = sv.add(diff.x()*100, diff.y()*100, diff.z()*100);
            ev = ev.subtract(diff.x()*100, diff.y()*100, diff.z()*100);

            //Draw a line from the where the players eyes are, and where theyre looking in transformed space
            IVertexBuilder buff = buffers.getBuffer(RenderType.lines());
            buff.vertex(pose, (float) sv.x, (float) sv.y, (float) sv.z).color(1f, 0, 0, 1).endVertex();
            buff.vertex(pose, (float) ev.x, (float) ev.y, (float) ev.z).color(0f, 1f, 0f, 1f).endVertex();

            //Draw a light blue line where the vector is hit in transformed space
            buff.vertex(pose, (float) this.hitRotated.x, (float) this.hitRotated.y, (float) this.hitRotated.z).color(0f, 1f, 1, 1F).endVertex();
            buff.vertex(pose, (float) this.hitRotated.x, (float) this.hitRotated.y+0.25F, (float) this.hitRotated.z).color(0f, 1f, 1f, 1F).endVertex();

            //Draw a yellow line where the vector is hit in real space (should be right in front of the mouse)
            Vector3d hitVec = result.getLocation();
            buff.vertex(pose, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z).color(1f, 1f, 0, 1F).endVertex();
            buff.vertex(pose, (float) hitVec.x, (float) hitVec.y+0.25F, (float) hitVec.z).color(1f, 1f, 0f, 1F).endVertex();

            //Draw a cubeoid of the transformed collision box
            RenderHelper.setupForFlatItems();
            AxisAlignedBB aabb = this.parent.box;
            RenderUtils.drawCubeoid(stack, new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ), buffers.getBuffer(RenderType.waterMask()));

            WorldRenderer.renderLineBox(stack, buff, aabb, 1, 0, 0, 1F);

            stack.popPose();
        }
    }
}
