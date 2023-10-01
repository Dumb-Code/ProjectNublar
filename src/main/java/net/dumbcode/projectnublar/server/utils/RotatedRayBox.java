package net.dumbcode.projectnublar.server.utils;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.*;

import javax.annotation.Nullable;
import java.util.Collections;

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

            return new Result(this, result, hitDir, start, end, startIn, endIn, hit, dist);
        }
        return null;
    }

    private void transform(Vector3f vec, Matrix4f mat) {
        Vector4f v = new Vector4f(vec);
        v.transform(mat);
        vec.set(v.x(), v.y(), v.z());
    }

    public Vector3f[] points(double x, double y, double z) {
        return points(RotatedRayBox.this.box, x, y, z);
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
                        )
                    );
                }
            }
        }
        for (Vector3f point : points) {
            RotatedRayBox.this.transform(point, RotatedRayBox.this.backwards);
            point.add((float) x, (float) y, (float) z);
        }
        return points;
    }

    public static class Builder {
        private final AxisAlignedBB box;
        private Vector3f origin = new Vector3f(0, 0, 0);
        private GuiGraphics matrix = new GuiGraphics();

        public Builder(AxisAlignedBB box) {
            this.box = box;
        }

        public Builder origin(double x, double y, double z) {
            this.origin = new Vector3f((float) x, (float) y, (float) z);
            return this;
        }

        public Builder rotate(double angle, float x, float y, float z) {
            this.matrix.mulPose(new Vector3f(x, y, z).rotation((float) angle));
            return this;
        }

        public RotatedRayBox build() {
            Matrix4f pose = this.matrix.last().pose();
            Matrix4f backwards = new Matrix4f(pose);
            backwards.invert();
            return new RotatedRayBox(this.box, this.origin, pose, backwards);
        }
    }

    @Value
    public static class Result {
        private final RotatedRayBox parent;
        private final BlockRayTraceResult result;
        private final Direction hitDir;
        private final Vector3f startRotated;
        private final Vector3f endRotated;
        private final IPosition start;
        private final IPosition end;
        private final Vector3d hitRotated;
        private final double distance;

        public void debugRender(GuiGraphics stack, IRenderTypeBuffer buffers, double x, double y, double z) {
            stack.pushPose();
            stack.translate(x + this.parent.origin.x(), y + this.parent.origin.y(), z + this.parent.origin.z());
            Matrix4f pose = stack.last().pose();


            Vector3d sv = new Vector3d(this.startRotated.x(), this.startRotated.y(), this.startRotated.z());
            Vector3d ev = new Vector3d(this.endRotated.x(), this.endRotated.y(), this.endRotated.z());

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
            WorldRenderer.renderLineBox(stack, buff, aabb, 1, 0, 0, 1F);

            RenderUtils.drawCubeoid(stack, new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ), buffers.getBuffer(RenderType.lightning()));

            //We need the lines type to begin buffering again.
            buffers.getBuffer(RenderType.lines());
            stack.popPose();
        }
    }
}
