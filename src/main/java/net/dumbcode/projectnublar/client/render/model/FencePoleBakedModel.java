package net.dumbcode.projectnublar.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FencePoleBakedModel extends FenceBakedModel {

    protected final IBakedModel delegate;

    public FencePoleBakedModel(TextureAtlasSprite texture, IBakedModel delegate) {
        super(texture);
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        List<BakedQuad> out = new ArrayList<>(super.getQuads(state, side, rand, extraData));

        Double rotation = extraData.getData(ProjectNublarModelData.FENCE_POLE_ROTATION_DEGS);
        if(rotation != null) {
            MatrixStack stack = new MatrixStack();
            stack.mulPose(Vector3f.YN.rotationDegrees(rotation.floatValue()));

            int size = DefaultVertexFormats.BLOCK.getIntegerSize();
            for (BakedQuad quad : this.delegate.getQuads(state, side, rand, extraData)) {
                BakedQuad copied = new BakedQuad(Arrays.copyOf(quad.getVertices(), quad.getVertices().length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
                int[] vertices = copied.getVertices();
                for (int v = 0; v < 4; v++) {
                    Vector4f vec = new Vector4f(
                        Float.intBitsToFloat(vertices[v*size]),
                        Float.intBitsToFloat(vertices[v*size+1]),
                        Float.intBitsToFloat(vertices[v*size+2]),
                        1F
                    );
                    vec.transform(stack.last().pose());
                    vertices[v*size] = Float.floatToRawIntBits(vec.x());
                    vertices[v*size+1] = Float.floatToRawIntBits(vec.y());
                    vertices[v*size+2] = Float.floatToRawIntBits(vec.z());
                }
                out.add(copied);
            }
        }
        return out;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.delegate.getParticleIcon();
    }
}
