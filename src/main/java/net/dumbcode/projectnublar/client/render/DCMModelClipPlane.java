package net.dumbcode.projectnublar.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Map;

public class DCMModelClipPlane {

    private static final DoubleBuffer BUFFER = BufferUtils.createDoubleBuffer(4);
    private final DCMModel model;
    private final Map<DCMModelRenderer, Vector3d[]> pointsMap = Maps.newHashMap();

    public DCMModelClipPlane(DCMModel model) {
        this.model = model;

        for (DCMModelRenderer cube : this.model.getAllCubes()) {
            int[] values = new int[] {0, 1};
            Vector3d[] points = new Vector3d[8];

            for (int xb : values) {
                for (int yb : values) {
                    for (int zb : values) {
                        //TODO: don't call this method, as that means doing it 8 times per cube. instead create a matrix stack and navigate through the cube tree recursively, applying the different transformations.
                        Vector3f partOrigin = DCMUtils.getModelPosAlpha(cube, xb, yb, zb);
                        points[(xb<<2)|(yb<<1)|zb] = new Vector3d(partOrigin);
                    }
                }
            }
            this.pointsMap.put(cube, points);
        }
    }

    public void render(MatrixStack stack, int light, ResourceLocation texture, double dist, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        //Note stuff WONT work when you change this
        Vector3f normal = new Vector3f(0, 1, 0);
        Vector3f movedNormal = normal.copy();
        movedNormal.mul((float) dist);
        Matrix4f pose = stack.last().pose();
        Matrix4f copy = pose.copy();
        if(!copy.invert()) {
            return;
        }
        copy.transpose();
        Vector4f vector4f = new Vector4f(normal.x(), normal.y(), normal.z(), (float) dist);
        vector4f.transform(copy);
        double[] plane = new double[]{vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w()};

        DoubleBuffer db = BUFFER.put(plane);
        db.flip();
        GL11.glClipPlane(GL11.GL_CLIP_PLANE0, db);
        GL11.glEnable(GL11.GL_CLIP_PLANE0);

        this.model.renderImmediate(stack, light, texture);

        GL11.glDisable(GL11.GL_CLIP_PLANE0);

        for (Map.Entry<DCMModelRenderer, Vector3d[]> entry : this.pointsMap.entrySet()) {
            if(entry.getKey().visible) {

                List<Vector3d> outlist = Lists.newArrayList();

                Vector3d[] rawPoints = entry.getValue();
                Vector3d[] points = new Vector3d[8];
                for (int i = 0; i < rawPoints.length; i++) {
                    points[i] = rawPoints[i].add(movedNormal.x(), movedNormal.y(), movedNormal.z());
                }
                getCheckPlaneCross(points, outlist, 0b100, 0b101, 0b111, 0b110);
                getCheckPlaneCross(points, outlist, 0b000, 0b001, 0b011, 0b010);
                getCheckPlaneCross(points, outlist, 0b011, 0b111);
                getCheckPlaneCross(points, outlist, 0b110, 0b010);
                getCheckPlaneCross(points, outlist, 0b001, 0b101);
                getCheckPlaneCross(points, outlist, 0b100, 0b000);

                RenderSystem.disableCull();
                RenderSystem.disableTexture();
                RenderSystem.enableDepthTest();
                if(outlist.size() == 4) {

                    outlist.sort((o1, o2) -> {
                        int compare = Double.compare(o2.z, o1.z);
                        return compare == 0 ? Double.compare(o2.x, o1.x) : compare;
                    });

                    BufferBuilder buff = Tessellator.getInstance().getBuilder();
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    for (int coord : new int[]{0, 1, 3, 2, 1, 0, 2, 3}) {
                        Vector3d vec = outlist.get(coord);
                        buff.vertex(pose, (float) vec.x - movedNormal.x(), (float) vec.y - movedNormal.y(), (float) vec.z - movedNormal.z()).color(r, g, b, 255).endVertex();
                    }
                    Tessellator.getInstance().end();
                }
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }

    }

    private void getCheckPlaneCross(Vector3d[] points, List<Vector3d> outlist, int... ints) {
        for (int i = 0; i < ints.length; i++) {
            int nextID = (i + 1) % ints.length;
            if (ints.length == 2 && i == 1) {
                break;
            }
            Vector3d vec = points[ints[i]];
            Vector3d next = points[ints[nextID]];
            //TODO: make work with not just y axis
            if(vec.y >= 0 != next.y >= 0) { //Crosses 0
                double alpha = vec.y / (vec.y - next.y);
                outlist.add(new Vector3d(vec.x + (next.x - vec.x) * alpha, 0, vec.z + (next.z - vec.z) * alpha));
            }
        }
    }
}
