package net.dumbcode.projectnublar.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GyrosphereRenderer extends EntityRenderer<GyrosphereVehicle> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png");
    private static List<GyrosphereSphere.Vertex> sphere;

    public GyrosphereRenderer(EntityRendererManager manager) {
        super(manager);
    }

    private static List<GyrosphereSphere.Vertex> getSphere() {
        if(sphere == null) {
            sphere = GyrosphereSphere.generate();
        }
        return sphere;
    }

    @Override
    public void render(GyrosphereVehicle entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        super.render(entity, entityYaw, partialTicks, stack, buffers, light);
        stack.pushPose();
        stack.translate(0, entity.getType().getHeight() / 2F, 0);

        if(entity.prevRotation != null && entity.rotation != null) {
            stack.mulPose(slerp(entity.prevRotation, entity.rotation, partialTicks));
        }

        float scale = entity.getBbWidth() / 2F;
        stack.scale(scale, scale, scale);

        MatrixStack.Entry last = stack.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();
        IVertexBuilder buffer = buffers.getBuffer(RenderType.entityTranslucent(new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png")));
        for (GyrosphereSphere.Vertex vertex : getSphere()) {
            Vector4f vec = new Vector4f(vertex.x, vertex.y, vertex.z, 1.0F);
            vec.transform(pose);
            buffer
                .vertex(vec.x(), vec.y(), vec.z())
                .color(1F, 1F, 1F, 1F)
                .uv(vertex.u, vertex.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, vertex.nx, vertex.ny, vertex.nz)
                .endVertex();
        }

        stack.popPose();
    }

    //Adapted from https://github.com/JOML-CI/JOML/blob/master/src/org/joml/Quaternionf.java
    private static Quaternion slerp(Quaternion current, Quaternion target, float partialTicks) {
        Quaternion dest = new Quaternion(0, 0, 0, 0);
        float cosom = current.i() * target.i() + current.j() * target.j() + current.k() * target.k() + current.r() * target.r();
        float absCosom = Math.abs(cosom);
        float scale0, scale1;
        if (1.0f - absCosom > 1E-6f) {
            float sinSqr = 1.0f - absCosom * absCosom;
            float sinom = (float) (1.0 / Math.sqrt(sinSqr));
            float omega = (float) Math.atan2(sinSqr * sinom, absCosom);
            scale0 = (float) (Math.sin((1.0 - partialTicks) * omega) * sinom);
            scale1 = (float) (Math.sin(partialTicks * omega) * sinom);
        } else {
            scale0 = 1.0f - partialTicks;
            scale1 = partialTicks;
        }
        scale1 = cosom >= 0.0f ? scale1 : -scale1;
        dest.set(
            scale0 * current.i() + scale1 * target.i(),
            scale0 * current.j() + scale1 * target.j(),
            scale0 * current.k() + scale1 * target.k(),
            scale0 * current.r() + scale1 * target.r()
        );
        return dest;
    }

    @Override
    public ResourceLocation getTextureLocation(GyrosphereVehicle p_110775_1_) {
        return new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png");
    }
}
