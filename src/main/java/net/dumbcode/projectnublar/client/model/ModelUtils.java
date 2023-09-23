package net.dumbcode.projectnublar.client.model;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.util.Arrays;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

public class ModelUtils {

    /**
     * Retextures the quad to the atlas sprite, offset by a specific amount
     * @param quad The original quad
     * @param sprite The texture to replace
     * @param offset The offset. Setting this to 0 will mean the quad will be at the exact same position
     *               as before, causing z-fighting if both quads are rendered. To fix this, apply a very small
     *               offset to physically move the vertexes the direction of the quad
     * @return The new remapped quad
     */
    public static BakedQuad retextureQuad(BakedQuad quad, TextureAtlasSprite sprite, float offset) {
        int[] data = Arrays.copyOf(quad.getVertices(), quad.getVertices().length);
        for (int i = 0; i < 4; i++) {
            int j = DefaultVertexFormats.BLOCK.getIntegerSize() * i;

            float x = intBitsToFloat(data[j]) + offset*quad.getDirection().getStepX();
            float y = intBitsToFloat(data[j+1]) + offset*quad.getDirection().getStepY();
            float z = intBitsToFloat(data[j+2]) + offset*quad.getDirection().getStepZ();

            data[j] = floatToRawIntBits(x);
            data[j+1] = floatToRawIntBits(y);
            data[j+2] = floatToRawIntBits(z);

            float ui;
            float vi;

            // This is figured out from trial and error, I think the discrepancy is due to
            // weird vanilla axis-flipping behaviour
            switch (quad.getDirection().getAxis()) {
                case X:
                    ui = z;
                    vi = 1-y;
                    break;
                case Y:
                default:
                    ui = x;
                    vi = z;
                    break;
                case Z:
                    ui = x;
                    vi = 1-y;
                    break;
            }

            data[j+4] = floatToRawIntBits(sprite.getU(ui*16F));
            data[j+5] = floatToRawIntBits(sprite.getV(vi*16F));

            data[j+6] = (240 << 16) | 240;
        }

        return new BakedQuad(data, -1, quad.getDirection(), sprite, quad.isShade());
    }

}
