package net.dumbcode.projectnublar.client.utils;

import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLLog;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

public class RenderUtils {
    public static void setupPointers(VertexFormat format) {
        int stride = format.getNextOffset();
        int offset = 0;
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION:
                    GlStateManager.glVertexPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    GL11.glNormalPointer(element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                    break;
                case COLOR:
                    GlStateManager.glColorPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glTexCoordPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                case PADDING:
                    break;
                default:
                    FMLLog.log.fatal("Unimplemented vanilla attribute upload: {}", element.getUsage().getDisplayName());
            }
            offset += element.getSize();
        }
    }

    public static void disableStates(VertexFormat format) {
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION:
                    GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    break;
                case NORMAL:
                    GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                case PADDING:
                    break;
                default:
                    FMLLog.log.fatal("Unimplemented vanilla attribute upload: {}", element.getUsage().getDisplayName());
            }
        }
    }

    public static void drawCubeoid(Vec3d s, Vec3d e) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
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
    }

    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urbx, double urby, double urbz, double urfx, double urfy, double urfz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drbx, double drby, double drbz, double drfx, double drfy, double drfz, double uu, double uv,double du, double dv, double lu, double lv,double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th,double td) {
        Vector3f xNorm = MathUtils.calcualeNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calcualeNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calcualeNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        buff.pos(urfx, urfy, urfz).tex(fu, fv).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(fu, fv+th).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(fu+td, fv+th).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(fu+td, fv).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(bu, bv).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(bu, bv+th).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(bu+td, bv+th).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(bu+td, bv).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(uu, uv).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(uu, uv+tw).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(uu+td, uv+tw).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(urfx, urfy, urfz).tex(uu+td, uv).normal(yNorm.x, yNorm.y, yNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(du, dv).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(du+td, dv+tw).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(du+td, dv+tw).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(du, dv).normal(-yNorm.x, -yNorm.y, -yNorm.z).endVertex();
        buff.pos(drfx, drfy, drfz).tex(ru, rv).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(urfx, urfy, urfz).tex(ru+th, rv).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(urbx, urby, urbz).tex(ru+th, rv+tw).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(drbx, drby, drbz).tex(ru, rv+tw).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
        buff.pos(ulfx, ulfy, ulfz).tex(lu, lv).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(dlfx, dlfy, dlfz).tex(lu+th, lv).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(dlbx, dlby, dlbz).tex(lu+th, lv+tw).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        buff.pos(ulbx, ulby, ulbz).tex(lu, lv+tw).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
        Tessellator.getInstance().draw();
    }
}
