package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Getter
public enum ConnectionType {
    LIGHT_STEEL(2, 3, 6/16F, 1/32F, 90F, 1/8F, 10),
    HIGH_SECURITY(1, 8, 1/2F, 1/16F, 0F, 2/8F, 15);
    private final double[] offsets;
    private final int height;
    private final float radius;
    private final float cableWidth;
    private final float rotationOffset;
    private final float halfSize;
    private final int lightLevel;
    private final ResourceLocation texture;

    @SideOnly(Side.CLIENT)
    public int listID;
    @SideOnly(Side.CLIENT)
    public VertexBuffer vbo;

    ConnectionType(int amount, int height, float radius, float cableWidth, float defaultRotation, float halfSize, int lightLevel) {
        this.offsets = new double[amount];
        this.height = height;
        this.radius = radius;
        this.cableWidth = cableWidth;
        this.rotationOffset = defaultRotation;
        this.halfSize = halfSize;
        this.lightLevel = lightLevel;
        this.texture = new ResourceLocation(ProjectNublar.MODID, "textures/blocks/" + this.name().toLowerCase() + "_electric_fence_pole.png");

        double off = 1D / (amount * 2);
        for (int i = 0; i < amount; i++) {
            this.offsets[i] = (i*2 + 1) * off;
        }
    }

    public static ConnectionType getType(int id) {
        return values()[id % values().length];
    }
}
