package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;

@Getter
public class RotatedRayBox {

    private final AxisAlignedBB box;
    private final Vec3d origin;
    private final Matrix4d forward;
    private final Matrix4d backwards;

    public RotatedRayBox(AxisAlignedBB box, Vec3d origin, Matrix4d forward, Matrix4d backwards) {
        this.box = box;
        this.origin = origin;
        this.forward = forward;
        this.backwards = backwards;
    }

    @Nullable
    public Result rayTrace(Vec3d startIn, Vec3d endIn) {
        Vector3d start = new Vector3d(startIn.x - this.origin.x, startIn.y - this.origin.y, startIn.z - this.origin.z);
        Vector3d end = new Vector3d(endIn.x - this.origin.x, endIn.y - this.origin.y, endIn.z - this.origin.z);
        this.forward.transform(start);
        this.forward.transform(end);

        Vec3d sv = new Vec3d(start.x, start.y, start.z);
        Vec3d ev = new Vec3d(end.x, end.y, end.z);

        Vec3d diff = sv.subtract(ev);

        //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
        sv = sv.addVector(diff.x*100, diff.y*100, diff.z*100);
        ev = ev.subtract(diff.x*100, diff.y*100, diff.z*100);

        RayTraceResult result = this.box.calculateIntercept(sv, ev);
        if(result != null) {
            EnumFacing hitDir = result.sideHit;
            Vec3d hit = result.hitVec;
            double dist = hit.squareDistanceTo(start.x, start.y, start.z);

            Vector3d hitvec = new Vector3d(hit.x, hit.y, hit.z);
            this.backwards.transform(hitvec);


            Vec3i vec = result.sideHit.getDirectionVec();
            Vector3d sidevec = new Vector3d(vec.getX(), vec.getY(), vec.getZ());
            this.backwards.transform(sidevec);

            result = new RayTraceResult(new Vec3d(hitvec.x, hitvec.y, hitvec.z), EnumFacing.getFacingFromVector((float) sidevec.x, (float) sidevec.y, (float) sidevec.z));
            result.hitInfo = dist;

            return new Result(this, result, hitDir, start, end, startIn, endIn, hit, dist);
        }
        return null;
    }

    public Vector3d[] points() {
        return points(RotatedRayBox.this.box);
    }

    public Vector3d[] points(AxisAlignedBB box) {
        int[] values = new int[] {0, 1};
        Vector3d[] points = new Vector3d[8];

        for (int xb : values) {
            for (int yb : values) {
                for (int zb : values) {
                    points[(xb << 2) + (yb << 1) + zb] = new Vector3d(xb == 1 ? box.maxX : box.minX, yb == 1 ? box.maxY : box.minY, zb == 1 ? box.maxZ : box.minZ);
                }
            }
        }
        for (Vector3d point : points) {
            RotatedRayBox.this.backwards.transform(point);
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
        private final RayTraceResult result;
        private final EnumFacing hitDir;
        private final Vector3d startRotated;
        private final Vector3d endRotated;
        private final Vec3d start;
        private final Vec3d end;
        private final Vec3d hitRotated;
        private final double distance;

        public void debugRender() {

            GlStateManager.depthMask(true);

            BufferBuilder buff = Tessellator.getInstance().getBuffer();

            //Draw a line from the where the players eyes are, and where theyre looking in transformed space
            buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buff.pos(this.startRotated.x, this.startRotated.y, this.startRotated.z).color(1f, 0, 0, 1).endVertex();
            buff.pos(this.endRotated.x, this.endRotated.y, this.endRotated.z).color(0f, 1f, 0f, 1f).endVertex();
            Tessellator.getInstance().draw();

            //Draw a light blue line where the vector is hit in transformed space
            buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buff.pos(this.hitRotated.x, this.hitRotated.y, this.hitRotated.z).color(0f, 1f, 1, 1F).endVertex();
            buff.pos(this.hitRotated.x, this.hitRotated.y+0.25, this.hitRotated.z).color(0f, 1f, 1f, 1F).endVertex();
            Tessellator.getInstance().draw();

            //Draw a yellow line where the vector is hit in real space (should be right in front of the mouse)
            buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buff.pos(result.hitVec.x, result.hitVec.y, result.hitVec.z).color(1f, 1f, 0, 1F).endVertex();
            buff.pos(result.hitVec.x, result.hitVec.y+0.25, result.hitVec.z).color(1f, 1f, 0f, 1F).endVertex();
            Tessellator.getInstance().draw();


            //Draw a cubeoid of the transformed collision box
            RenderHelper.enableStandardItemLighting();
            GlStateManager.color(1,1,1,1);
            AxisAlignedBB aabb = this.parent.box;
            RenderUtils.drawCubeoid(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ));
            GlStateManager.disableLighting();
            RenderGlobal.drawSelectionBoundingBox(aabb, 1,0,0,1F);

        }
    }
}
