package net.dumbcode.projectnublar.client.render.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.vehicles.GyrosphereVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Quaternion;

/**
 * Minecrafts blending order is fucked up. If this were to be an entity renderer,
 * the either the vehicle would use translucent textures, or it woulnt look good
 */
@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class GyrosphereRenderer {

    private static int sphereID = -1;

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

    @SubscribeEvent
    public static void worldRenderLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.enableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.shadeModel(7425);
        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        Minecraft.getMinecraft().getRenderManager().setRenderPosition(TileEntityRendererDispatcher.staticPlayerX, TileEntityRendererDispatcher.staticPlayerY, TileEntityRendererDispatcher.staticPlayerZ);
        Minecraft.getMinecraft().entityRenderer.enableLightmap();
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/entities/test_out.png"));

        for (Entity e : player.world.loadedEntityList) {
            if(e.getClass() == GyrosphereVehicle.class) {
                GyrosphereVehicle entity = (GyrosphereVehicle) e;
                GlStateManager.pushMatrix();

                double entityX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
                double entityY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
                double entityZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
                int i = entity.getBrightnessForRender();
                if (entity.isBurning()) {
                    i = 15728880;
                }
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
                GlStateManager.translate(entityX, entityY, entityZ);
                GlStateManager.translate(0, entity.height / 2, 0);


                Quaternion pre = entity.prevRotation;
                Quaternion cur = entity.rotation;

                GlStateManager.rotate(slerp(pre, cur, partialTicks));

                GlStateManager.scale(entity.width/2F,entity.width/2F,entity.width/2F);
                GlStateManager.callList(getSphereID());
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
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
}
