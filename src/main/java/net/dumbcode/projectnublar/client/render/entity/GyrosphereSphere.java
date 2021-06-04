package net.dumbcode.projectnublar.client.render.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class GyrosphereSphere {
    private static final float PI = (float) Math.PI;

    public static List<Vertex> generate() {
        float radius = 1;
        int slices = 15;
        int stacks = 15;

        List<Vertex> vertices = new ArrayList<>();

        float rho, drho, theta, dtheta;
        float x, y, z;
        float s, t, ds, dt;
        int i, j, imin, imax;

        for (float nsign : new float[] { -1.0F, 1.0F }) {

            drho = PI / stacks;
            dtheta = 2.0f * PI / slices;

            ds = 1.0f / slices;
            dt = 1.0f / stacks;
            t = 1.0f; // because loop now runs from 0
            imin = 0;
            imax = stacks;

            // draw intermediate stacks as quad strips
            for (i = imin; i < imax; i++) {
                rho = i * drho;
                s = 0.0f;
                for (j = 0; j <= slices; j++) {
                    theta = (j == slices) ? 0.0f : j * dtheta;

                    x = -sin(theta) * sin(rho);
                    y = cos(theta) * sin(rho);
                    z = nsign * cos(rho);

                    vertices.add(new Vertex(
                        x * radius, y * radius, z * radius,
                        s, t,
                        x * nsign, y * nsign, z * nsign
                    ));

                    x = -sin(theta) * sin(rho + drho);
                    y = cos(theta) * sin(rho + drho);
                    z = nsign * cos(rho + drho);

                    vertices.add(new Vertex(
                        x * radius, y * radius, z * radius,
                        s, t - dt,
                        x * nsign, y * nsign, z * nsign
                    ));

                    s += ds;
                }
                t -= dt;
            }
        }

        List<Vertex> outList = new ArrayList<>();
        for (int v = 0; v < vertices.size(); v+=4) {
            outList.add(vertices.get(v));
            outList.add(vertices.get(v+1));
            outList.add(vertices.get(v+2));

            outList.add(vertices.get(v+3));
            outList.add(vertices.get(v));
            outList.add(vertices.get(v+2));
        }
        return outList;
    }

    private static float sin(float f) {
        return MathHelper.sin(f);
    }

    private static float cos(float f) {
        return MathHelper.cos(f);
    }
    @RequiredArgsConstructor
    public static class Vertex {
        public final float x, y, z;
        public final float u, v;
        public final float nx, ny, nz;
    }
}
