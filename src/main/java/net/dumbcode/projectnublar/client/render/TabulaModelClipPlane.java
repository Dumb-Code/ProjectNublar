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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.DoubleBuffer;
import java.util.List;
import java.util.Map;

public class TabulaModelClipPlane {
    private final DCMModel model;
    private final Map<DCMModelRenderer, Vector3d[]> pointsMap = Maps.newHashMap();

    public TabulaModelClipPlane(DCMModel model) {
        this.model = model;

        for (DCMModelRenderer cube : this.model.getAllCubes()) {
            int[] values = new int[] {0, 1};
            Vector3d[] points = new Vector3d[8];

            for (int xb : values) {
                for (int yb : values) {
                    for (int zb : values) {
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
        double[] plane = new double[]{0,1,0,dist};

        DoubleBuffer db = BufferUtils.createDoubleBuffer(8).put(plane);
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
                    points[i] = rawPoints[i].add(0, plane[3], 0);
                }
                getCheckPlaneCross(points, outlist, 0b100, 0b101, 0b111, 0b110);
                getCheckPlaneCross(points, outlist, 0b000, 0b001, 0b011, 0b010);
                getCheckPlaneCross(points, outlist, 0b011, 0b111);
                getCheckPlaneCross(points, outlist, 0b110, 0b010);
                getCheckPlaneCross(points, outlist, 0b001, 0b101);
                getCheckPlaneCross(points, outlist, 0b100, 0b000);

                RenderSystem.disableCull();
                RenderSystem.disableTexture();
                if(outlist.size() == 4) {

                    outlist.sort((o1, o2) -> {
                        int compare = Double.compare(o2.z, o1.z);
                        return compare == 0 ? Double.compare(o2.x, o1.x) : compare;
                    });

                    BufferBuilder buff = Tessellator.getInstance().getBuilder();
                    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    for (int coord : new int[]{0, 1, 3, 2, 1, 0, 2, 3}) {
                        Vector3d vec = outlist.get(coord);
                        buff.vertex(vec.x, vec.y - plane[3], vec.z).color(r, g, b, 255).endVertex();
                    }
                    Tessellator.getInstance().end();
                }
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
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
            if(vec.y >= 0 != next.y >= 0) { //Crosses 0
                double alpha = vec.y / (vec.y - next.y);
                outlist.add(new Vector3d(vec.x + (next.x - vec.x) * alpha, 0, vec.z + (next.z - vec.z) * alpha));
            }
        }
    }
}
