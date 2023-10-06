package net.dumbcode.projectnublar.client.render.entity;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.math.Mth;

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

        for (float nsign : new float[] { 1.0F }) {

            drho = PI / stacks;
            dtheta = 2.0f * PI / slices;

            ds = 1.0f / slices;
            dt = 1.0f / stacks;
            t = 1.0f; // because loop now runs from 0
            imin = 0;
            imax = stacks;

            // draw intermediate stacks as quad strips
            for (i = imin; i < imax; i++) {

                List<Vertex> strip = new ArrayList<>();
                rho = i * drho;
                s = 0.0f;
                for (j = 0; j <= slices; j++) {
                    theta = (j == slices) ? 0.0f : j * dtheta;

                    x = -sin(theta) * sin(rho);
                    y = cos(theta) * sin(rho);
                    z = nsign * cos(rho);

                    strip.add(new Vertex(
                        x * radius, y * radius, z * radius,
                        s, t,
                        x * nsign, y * nsign, z * nsign
                    ));

                    x = -sin(theta) * sin(rho + drho);
                    y = cos(theta) * sin(rho + drho);
                    z = nsign * cos(rho + drho);

                    strip.add(new Vertex(
                        x * radius, y * radius, z * radius,
                        s, t - dt,
                        x * nsign, y * nsign, z * nsign
                    ));

                    s += ds;
                }

                //We need to convert from a quad strip to just quads.
                for (int si = 2; si < strip.size(); si += 2) {
                    Vertex a = strip.get(si-2);
                    Vertex b = strip.get(si-1);
                    Vertex c = strip.get(si);
                    Vertex d = strip.get(si+1);

                    vertices.add(b);
                    vertices.add(a);
                    vertices.add(c);
                    vertices.add(d);
                }

                t -= dt;
            }
        }

        return vertices;
    }

    private static float sin(float f) {
        return Mth.sin(f);
    }

    private static float cos(float f) {
        return Mth.cos(f);
    }
    @RequiredArgsConstructor
    public static class Vertex {
        public final float x, y, z;
        public final float u, v;
        public final float nx, ny, nz;
    }
}
