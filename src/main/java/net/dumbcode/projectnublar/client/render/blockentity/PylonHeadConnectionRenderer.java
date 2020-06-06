package net.dumbcode.projectnublar.client.render.blockentity;

import lombok.Getter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.*;
import javax.vecmath.Vector3d;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class PylonHeadConnectionRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();
    public static final int ITERATIONS_PER_BLOCK = 2;

    private static final Matrix3d RESULT_MATRIX = new Matrix3d();
    private static final Matrix3d TEMP_MATRIX = new Matrix3d();


    @SubscribeEvent
    public static void renderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();

        Entity e = MC.getRenderViewEntity();
        if (e == null) {
            return;
        }

        MC.profiler.startSection(ProjectNublar.MODID + "_pylon_connection_render");
        Set<PylonHeadBlockEntity.Connection> connections = new HashSet<>();
        for (TileEntity te : MC.world.loadedTileEntityList) {
            if (te instanceof PylonHeadBlockEntity) {
                connections.addAll(((PylonHeadBlockEntity) te).getConnections());
            }
        }
        double posX = e.lastTickPosX + (e.posX - e.lastTickPosX) * partialTicks;
        double posY = e.lastTickPosY + (e.posY - e.lastTickPosY) * partialTicks;
        double posZ = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-posX, -posY, -posZ);

        RenderHelper.enableStandardItemLighting();
        MC.entityRenderer.enableLightmap();
        MC.renderEngine.bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.glLineWidth(2);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        for (PylonHeadBlockEntity.Connection connection : connections) {
            TileEntity entity = MC.world.getTileEntity(connection.getFrom());
            Vec3d from = new Vec3d(connection.getFrom()).add(0.5, 0.5, 0.5);
            Vec3d to = new Vec3d(connection.getTo()).add(0.5, 0.5, 0.5);
            Vec3d diff = to.subtract(from);

            double dist = from.distanceTo(to);
            int iterations = (int) (dist * ITERATIONS_PER_BLOCK);
            for (int i = 0; i < iterations; i++) {
                float current = (float) i / iterations;
                float next = (float) (i+1) / iterations;

                Vec3d cPos = new Vec3d(from.x + (to.x - from.x) * current, connection.beizerCurve(current)+0.5, from.z + (to.z - from.z) * current);
                Vec3d nPos = new Vec3d(from.x + (to.x - from.x) * next, connection.beizerCurve(next)+0.5, from.z + (to.z - from.z) * next);

                DirVec c = DirVec.get(connection.beizerCurveGradient(current), diff, 0.025F);
                DirVec n = DirVec.get(connection.beizerCurveGradient(next), diff, 0.025F);

                double[] points = new double[8*3];
                for (int p = 0; p < 8; p++) {
                    boolean b0 = (p & 1) == 1;
                    boolean b1 = (p & 2) == 2;
                    boolean b2 = (p & 4) == 4;

                    DirVec v = b0 ? n : c;

                    double x = (b0 ? nPos.x : cPos.x) + (b1 ? v.l.x : -v.l.x) + (b2 ? v.u.x : -v.u.x);
                    double y = (b0 ? nPos.y : cPos.y) + (b1 ? v.l.y : -v.l.y) + (b2 ? v.u.y : -v.u.y);
                    double z = (b0 ? nPos.z : cPos.z) + (b1 ? v.l.z : -v.l.z) + (b2 ? v.u.z : -v.u.z);

                    points[p*3] = x;
                    points[p*3+1] = y;
                    points[p*3+2] = z;
                }

                int light = MC.world.getCombinedLight(new BlockPos(cPos.add(nPos).scale(0.5F)), 0);
                int s = light % 65536;
                int b = light / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, s, b);

                if(ProjectNublar.DEBUG) {
                    Random testRandom = new Random(entity instanceof PylonHeadBlockEntity ? ((PylonHeadBlockEntity) entity).getNetworkUUID().getLeastSignificantBits() : 0);
                    GlStateManager.color(testRandom.nextFloat(), testRandom.nextFloat(), testRandom.nextFloat(), 1F);
                }


                RenderUtils.drawSpacedCube(
                    points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7],
                    points[8], points[9], points[10], points[11], points[12], points[13], points[14], points[15],
                    points[16], points[17], points[18], points[19], points[20], points[21], points[22], points[23],
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1);
            }
        }

        GlStateManager.popMatrix();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        RenderHelper.disableStandardItemLighting();
        MC.entityRenderer.disableLightmap();

        MC.profiler.endSection();
    }

    @Getter
    private static class DirVec {
        private final Vector3d f;
        private final Vector3d u;
        private final Vector3d l;

        private DirVec(Vector3d f, Vector3d u, Vector3d l) {
            this.f = new Vector3d(f);
            this.u = new Vector3d(u);
            this.l = new Vector3d(l);
        }

        private static DirVec get(double beizerCurveGradient, Vec3d diff, float scale) {
            RESULT_MATRIX.setIdentity();

            TEMP_MATRIX.rotZ(Math.atan(beizerCurveGradient / Math.sqrt(diff.x*diff.x + diff.z*diff.z)));
            RESULT_MATRIX.mul(TEMP_MATRIX, RESULT_MATRIX);

            TEMP_MATRIX.rotY(-Math.atan2(diff.z, diff.x));
            RESULT_MATRIX.mul(TEMP_MATRIX, RESULT_MATRIX);

            Vector3d f = new Vector3d(scale, 0, 0);
            RESULT_MATRIX.transform(f);

            Vector3d u = new Vector3d(0, scale, 0);
            RESULT_MATRIX.transform(u);

            Vector3d l = new Vector3d(0, 0, scale);
            RESULT_MATRIX.transform(l);

            return new DirVec(f, u, l);
        }
    }
}
