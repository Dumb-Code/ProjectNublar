package net.dumbcode.projectnublar.client.render.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Quaternion;

import javax.annotation.Nullable;

public class GyrosphereRenderer extends Render<GyrosphereVehicle> {

    private static int sphereID = -1;

    public GyrosphereRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    private static int getSphereID() {
        if(sphereID == -1) {
            Sphere sphere = new Sphere();
            sphere.setTextureFlag(true);
            sphere.setDrawStyle(GLU.GLU_FILL);
            sphere.setNormals(GLU.GLU_SMOOTH);
            sphereID = GlStateManager.glGenLists(1);
            GlStateManager.glNewList(sphereID, GL11.GL_COMPILE);
            sphere.draw(1, 15, 15); //TODO: configurable slices and stacks
            sphere.setOrientation(GLU.GLU_INSIDE);
            sphere.draw(1, 15, 15);
            GlStateManager.glEndList();
        }
        return sphereID;
    }

    @Override
    public void doRender(GyrosphereVehicle entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0, entity.height / 2, 0);

        if(entity.prevRotation != null && entity.rotation != null) {
            GlStateManager.rotate(slerp(entity.prevRotation, entity.rotation, partialTicks));
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png"));
        GlStateManager.scale(entity.width/2F,entity.width/2F,entity.width/2F);
        GlStateManager.callList(getSphereID());
        GlStateManager.popMatrix();
    }

    //Adapted from https://github.com/JOML-CI/JOML/blob/master/src/org/joml/Quaternionf.java
    private static Quaternion slerp(Quaternion current, Quaternion target, float partialTicks) {
        Quaternion dest = new Quaternion();
        float cosom = current.x * target.x + current.y * target.y + current.z * target.z + current.w * target.w;
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
        dest.x = scale0 * current.x + scale1 * target.x;
        dest.y = scale0 * current.y + scale1 * target.y;
        dest.z = scale0 * current.z + scale1 * target.z;
        dest.w = scale0 * current.w + scale1 * target.w;
        return dest;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(GyrosphereVehicle entity) {
        return new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png");
    }
}
