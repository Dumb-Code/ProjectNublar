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

    private final int sphereID;

//    private final FloatBuffer FLOAT_16 = BufferUtils.createFloatBuffer(16);

    public GyrosphereRenderer(RenderManager renderManager) {
        super(renderManager);
        Sphere sphere = new Sphere();
        sphere.setTextureFlag(true);
        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GLU.GLU_SMOOTH);
        this.sphereID = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(this.sphereID, GL11.GL_COMPILE);
        sphere.draw(1, 30, 30);
        sphere.setOrientation(GLU.GLU_INSIDE);
        sphere.draw(1, 30, 30);
        GlStateManager.glEndList();
    }

    @Override//previousCurrent + (current - previousCurrent) * partialTicks
    public void doRender(GyrosphereVehicle entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0, entity.height / 2, 0);

        Quaternion pre = entity.prevRotation;
        Quaternion cur = entity.rotation;

        GlStateManager.rotate(new Quaternion(
                pre.x + (cur.x - pre.x) * partialTicks,
                pre.y + (cur.y - pre.y) * partialTicks,
                pre.z + (cur.z - pre.z) * partialTicks,
                pre.w + (cur.w - pre.w) * partialTicks
        ));

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png"));
        GlStateManager.scale(entity.width/2F,entity.width/2F,entity.width/2F);
        GlStateManager.callList(this.sphereID);
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(GyrosphereVehicle entity) {
        return null;
    }
}
