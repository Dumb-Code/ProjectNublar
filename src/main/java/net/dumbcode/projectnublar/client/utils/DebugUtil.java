package net.dumbcode.projectnublar.client.utils;

import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class DebugUtil {
    public static void renderFenceCollision(DrawBlockHighlightEvent event) {
        GlStateManager.enableCull();

        RayTraceResult target = event.getTarget();
        BlockElectricFence.Chunk chunk = (BlockElectricFence.Chunk) target.hitInfo;
        Connection.Cache cache = chunk.getConnection().getCache(chunk.getConnectionID());
        double[] in = cache.getIn();
        AxisAlignedBB aabb = chunk.getAabb();

        //Mirror what the matrix calculation does
        Vec3d start = Minecraft.getMinecraft().player.getPositionEyes(event.getPartialTicks());
        Vec3d vec3d1 = Minecraft.getMinecraft().player.getLook(event.getPartialTicks());
        Vec3d end = start.addVector(vec3d1.x * 5, vec3d1.y * 5, vec3d1.z * 5);
        Matrix4d rotymat = new Matrix4d();
        rotymat.rotY(in[1] == in[0] ? Math.PI*1.5D : Math.atan((in[3] - in[2]) / (in[1] - in[0])));
        Matrix4d rotzmat = new Matrix4d();
        rotzmat.rotZ(Math.atan((in[5] - in[4]) / cache.getXZlen()));
        Vector3d startvec = new Vector3d(start.x - in[0], start.y - in[4], start.z - in[2]);
        Vector3d endvec = new Vector3d(end.x - in[0], end.y - in[4], end.z - in[2]);
        Vector3d hitvec = new Vector3d(target.hitVec.x, target.hitVec.y, target.hitVec.z);

        GlStateManager.depthMask(true);
        rotymat.transform(startvec);
        rotymat.transform(endvec);
        rotymat.transform(hitvec);
        rotzmat.transform(startvec);
        rotzmat.transform(endvec);
        rotzmat.transform(hitvec);

        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        //Draw a line from the where the players eyes are, and where theyre looking in transformed space
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(startvec.x, startvec.y, startvec.z).color(1f, 0, 0, 1).endVertex();
        buff.pos(endvec.x, endvec.y, endvec.z).color(0f, 1f, 0f, 1f).endVertex();
        Tessellator.getInstance().draw();

        //Draw a light blue line where the vector is hit in transformed space
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(hitvec.x, hitvec.y, hitvec.z).color(0f, 1f, 1, 1F).endVertex();
        buff.pos(hitvec.x, hitvec.y+0.25, hitvec.z).color(0f, 1f, 1f, 1F).endVertex();
        Tessellator.getInstance().draw();

        //Draw a yellow line where the vector is hit in real space (should be right in front of the mouse)
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(target.hitVec.x, target.hitVec.y, target.hitVec.z).color(1f, 1f, 0, 1F).endVertex();
        buff.pos(target.hitVec.x, target.hitVec.y+0.25, target.hitVec.z).color(1f, 1f, 0f, 1F).endVertex();
        Tessellator.getInstance().draw();


        //Draw a cubeoid of the transformed collision box
        Vector3d s = new Vector3d(aabb.minX, aabb.minY, aabb.minZ);
        Vector3d e = new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1,1,1,1);
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL);
        buff.pos(s.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.pos(e.x, e.y, e.z).normal(0, 1, 0).endVertex();
        buff.pos(e.x, e.y, s.z).normal(0, 1, 0).endVertex();
        buff.pos(s.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.pos(s.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, s.y, s.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, s.y, e.z).normal(0, -1, 0).endVertex();
        buff.pos(e.x, e.y, e.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, s.y, e.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, s.y, s.z).normal(1, 0, 0).endVertex();
        buff.pos(e.x, e.y, s.z).normal(1, 0, 0).endVertex();
        buff.pos(s.x, s.y, e.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, s.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, s.y, s.z).normal(-1, 0, 0).endVertex();
        buff.pos(s.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(s.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(e.x, s.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(e.x, e.y, e.z).normal(0, 0, 1).endVertex();
        buff.pos(s.x, s.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(s.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(e.x, e.y, s.z).normal(0, 0, -1).endVertex();
        buff.pos(e.x, s.y, s.z).normal(0, 0, -1).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.disableLighting();
        RenderGlobal.drawSelectionBoundingBox(aabb, 1,0,0,1F);
    }
}
