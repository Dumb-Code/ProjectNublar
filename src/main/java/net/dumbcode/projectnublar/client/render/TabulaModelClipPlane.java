package net.dumbcode.projectnublar.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.model.InfoTabulaModel;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Map;

public class TabulaModelClipPlane {
    private final TabulaModel model;
    private final Map<AdvancedModelRenderer, Vec3d[]> pointsMap = Maps.newHashMap();

    public TabulaModelClipPlane(TabulaModel model) {
        this.model = model;

        for (AdvancedModelRenderer cube : this.model.getCubes().values()) {
            int[] values = new int[] {0, 1};
            Vec3d[] points = new Vec3d[8];

            for (int xb : values) {
                for (int yb : values) {
                    for (int zb : values) {

                        InfoTabulaModel infoModel = (InfoTabulaModel) model;
                        int[] dimensions = infoModel.getDimension(cube);
                        ModelBox box = ObfuscationReflectionHelper.<List<ModelBox>, ModelRenderer>getPrivateValue(ModelRenderer.class, cube, "cubeList", "field_78804" + "_l").get(0); //TODO: remove this god awful method of getting the offsets

                        Point3d endPoint = new Point3d((box.posX1 + dimensions[0] * xb) / 16F, (box.posY1 + dimensions[1] * yb) / -16F, (box.posZ1 + dimensions[2] * zb) / -16F);

                        Matrix4d boxTranslate = new Matrix4d();
                        Matrix4d boxRotateX = new Matrix4d();
                        Matrix4d boxRotateY = new Matrix4d();
                        Matrix4d boxRotateZ = new Matrix4d();
                        boxTranslate.set(new Vector3d(cube.rotationPointX / 16, -cube.rotationPointY / 16, -cube.rotationPointZ / 16));
                        boxRotateX.rotX(cube.rotateAngleX);
                        boxRotateY.rotY(-cube.rotateAngleY);
                        boxRotateZ.rotZ(-cube.rotateAngleZ);
                        boxRotateX.transform(endPoint);
                        boxRotateY.transform(endPoint);
                        boxRotateZ.transform(endPoint);
                        boxTranslate.transform(endPoint);

                        Vec3d partOrigin = cube.getModelPos(cube, new Vec3d(endPoint.x, endPoint.y, endPoint.z));
                        points[(xb<<2)|(yb<<1)|zb] = new Vec3d(partOrigin.x, -partOrigin.y, -partOrigin.z);
                    }
                }
            }
            this.pointsMap.put(cube, points);
        }
    }

    public void render(double dist, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double[] plane = new double[]{0,-1,0,dist};

        DoubleBuffer db = BufferUtils.createDoubleBuffer(8).put(plane);
        db.flip();
        GL11.glClipPlane(GL11.GL_CLIP_PLANE0, db);
        GL11.glEnable(GL11.GL_CLIP_PLANE0);

        MoreTabulaUtils.renderModelWithoutChangingPose(this.model, 1/16F);

        GL11.glDisable(GL11.GL_CLIP_PLANE0);

        for (Map.Entry<AdvancedModelRenderer, Vec3d[]> entry : this.pointsMap.entrySet()) {
            if(!entry.getKey().isHidden) {

                List<Vec3d> outlist = Lists.newArrayList();

                Vec3d[] rawPoints = entry.getValue();
                Vec3d[] points = new Vec3d[8];
                for (int i = 0; i < rawPoints.length; i++) {
                    points[i] = rawPoints[i].subtract(0, plane[3], 0);
                }
                getCheckPlaneCross(points, outlist, 0b100, 0b101, 0b111, 0b110);
                getCheckPlaneCross(points, outlist, 0b000, 0b001, 0b011, 0b010);
                getCheckPlaneCross(points, outlist, 0b011, 0b111);
                getCheckPlaneCross(points, outlist, 0b110, 0b010);
                getCheckPlaneCross(points, outlist, 0b001, 0b101);
                getCheckPlaneCross(points, outlist, 0b100, 0b000);

                GlStateManager.disableCull();
                GlStateManager.disableTexture2D();
                if(outlist.size() == 4) {

                    outlist.sort((o1, o2) -> {
                        int compare = Double.compare(o2.z, o1.z);
                        return compare == 0 ? Double.compare(o2.x, o1.x) : compare;
                    });

                    BufferBuilder buff = Tessellator.getInstance().getBuffer();
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    for (int coord : new int[]{0, 1, 3, 2, 1, 0, 2, 3}) {
                        Vec3d vec = outlist.get(coord);
                        buff.pos(vec.x, vec.y + plane[3], vec.z).color(r, g, b, 255).endVertex();
                    }
                    Tessellator.getInstance().draw();
                }
                GlStateManager.enableCull();
                GlStateManager.enableTexture2D();
            }
        }

    }

    private void getCheckPlaneCross(Vec3d[] points, List<Vec3d> outlist, int... ints) {
        for (int i = 0; i < ints.length; i++) {
            int nextID = (i + 1) % ints.length;
            if (ints.length == 2 && i == 1) {
                break;
            }
            Vec3d vec = points[ints[i]];
            Vec3d next = points[ints[nextID]];
            if(vec.y >= 0 != next.y >= 0) { //Crosses 0
                double alpha = vec.y / (vec.y - next.y);
                outlist.add(new Vec3d(vec.x + (next.x - vec.x) * alpha, 0, vec.z + (next.z - vec.z) * alpha));
            }
        }
    }
}
