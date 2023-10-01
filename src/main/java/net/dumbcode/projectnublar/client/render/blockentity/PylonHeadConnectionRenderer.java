package net.dumbcode.projectnublar.client.render.blockentity;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Vector3d;
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ProjectNublar.MODID)
public class PylonHeadConnectionRenderer {

    private static final Minecraft MC = Minecraft.getInstance();
    public static final int ITERATIONS_PER_BLOCK = 2;

    private static final Matrix3f RESULT_MATRIX = new Matrix3f();
    private static final Matrix3f TEMP_MATRIX = new Matrix3f();

    private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectNublar.MODID, "textures/block/electric_fence.png");

    @SubscribeEvent
    public static void renderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();


        MC.getProfiler().push(ProjectNublar.MODID + "_pylon_connection_render");
        Set<PylonHeadBlockEntity.Connection> connections = new HashSet<>();
        for (TileEntity te : MC.level.blockEntityList) {
            if (te instanceof PylonHeadBlockEntity) {
                connections.addAll(((PylonHeadBlockEntity) te).getConnections());
            }
        }

        ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3d pos = info.getPosition();

        GuiGraphics stack = event.getGuiGraphics();
        stack.pushPose();
        stack.translate(-pos.x, -pos.y, -pos.z);

        RenderHelper.setupForFlatItems();
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.lineWidth(2);

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);

        for (PylonHeadBlockEntity.Connection connection : connections) {
            TileEntity entity = MC.level.getBlockEntity(connection.getFrom());
            Vector3d from = new Vector3d(connection.getFrom().getX(), connection.getFrom().getY(), connection.getFrom().getZ()).add(0.5, 0.5, 0.5);
            Vector3d to = new Vector3d(connection.getTo().getX(), connection.getTo().getY(), connection.getTo().getZ()).add(0.5, 0.5, 0.5);
            Vector3d diff = to.subtract(from);

            double dist = from.distanceTo(to);
            int iterations = (int) Math.ceil(dist * ITERATIONS_PER_BLOCK);
            for (int i = 0; i < iterations; i+=1) {
                float current = (float) i / iterations;
                float next = (float) (i+1) / iterations;

                Vector3d cPos = new Vector3d(from.x + (to.x - from.x) * current, connection.beizerCurve(current)+0.75, from.z + (to.z - from.z) * current);
                Vector3d nPos = new Vector3d(from.x + (to.x - from.x) * next, connection.beizerCurve(next)+0.75, from.z + (to.z - from.z) * next);

                double segmentDiff = nPos.subtract(cPos).length();

                DirVec c = DirVec.get(connection.beizerCurveGradient(current), diff, 0.025F);
                DirVec n = DirVec.get(connection.beizerCurveGradient(next), diff, 0.025F);

                float[] points = new float[8*3];
                for (int p = 0; p < 8; p++) {
                    boolean b0 = (p & 1) == 1;
                    boolean b1 = (p & 2) == 2;
                    boolean b2 = (p & 4) == 4;

                    DirVec v = b0 ? n : c;

                    float x = (float) ((b0 ? nPos.x() : cPos.x()) + (b1 ? v.l.x() : -v.l.x()) + (b2 ? v.u.x() : -v.u.x()));
                    float y = (float) ((b0 ? nPos.y() : cPos.y()) + (b1 ? v.l.y() : -v.l.y()) + (b2 ? v.u.y() : -v.u.y()));
                    float z = (float) ((b0 ? nPos.z() : cPos.z()) + (b1 ? v.l.z() : -v.l.z()) + (b2 ? v.u.z() : -v.u.z()));

                    points[p*3] = x;
                    points[p*3+1] = y;
                    points[p*3+2] = z;
                }

                int light = WorldRenderer.getLightColor(MC.level, new BlockPos(cPos.add(nPos).scale(0.5F)));

                Random random = new Random(entity instanceof PylonHeadBlockEntity ? ((PylonHeadBlockEntity) entity).getNetworkUUID().getLeastSignificantBits() : 0);
                float r = 1;
                float g = 1;
                float b = 1;
                float a = 1;
                if(ProjectNublar.DEBUG) {
                    r = random.nextFloat();
                    g = random.nextFloat();
                    b = random.nextFloat();
                    a = random.nextFloat();
                }

                float[] uvs = new float[12];
                for (int ui = 0; ui < uvs.length; ui++) {
                    uvs[ui] = random.nextInt(16)/32F;
                }

                IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().renderBuffers().bufferSource();
                IVertexBuilder buffer = buffers.getBuffer(RenderType.entitySolid(TEXTURE));
                RenderUtils.drawSpacedCube(stack, buffer, r, g, b, a, light, OverlayTexture.NO_OVERLAY,
                    points[0], points[1], points[2], points[3], points[4], points[5], points[6], points[7],
                    points[8], points[9], points[10], points[11], points[12], points[13], points[14], points[15],
                    points[16], points[17], points[18], points[19], points[20], points[21], points[22], points[23],
                    uvs[0], uvs[1], uvs[2], uvs[3], uvs[4], uvs[5], uvs[6], uvs[7], uvs[8], uvs[9], uvs[10], uvs[11], Math.round(segmentDiff * 32F) / 64F, 1/32F, 1/32F);
                buffers.endBatch();
            }
        }

        stack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderHelper.turnOff();
//        MC.entityRenderer.disableLightmap();

        MC.getProfiler().pop();
    }

    @Getter
    private static class DirVec {
        private final Vector3f f;
        private final Vector3f u;
        private final Vector3f l;

        private DirVec(Vector3f f, Vector3f u, Vector3f l) {
            this.f = f.copy();
            this.u = u.copy();
            this.l = l.copy();
        }

        private static DirVec get(double beizerCurveGradient, Vector3d diff, float scale) {
            RESULT_MATRIX.setIdentity();
            RESULT_MATRIX.mul(Vector3f.YP.rotation((float) -Math.atan2(diff.z, diff.x)));
            RESULT_MATRIX.mul(Vector3f.ZP.rotation((float) Math.atan(beizerCurveGradient / Math.sqrt(diff.x*diff.x + diff.z*diff.z))));

            Vector3f f = new Vector3f(scale, 0, 0);
            f.transform(RESULT_MATRIX);

            Vector3f u = new Vector3f(0, scale, 0);
            u.transform(RESULT_MATRIX);

            Vector3f l = new Vector3f(0, 0, scale);
            l.transform(RESULT_MATRIX);

            return new DirVec(f, u, l);
        }
    }
}
