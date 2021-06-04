package net.dumbcode.projectnublar.client.render.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.client.BakedQuadGenerator;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FenceBakedModel implements IDynamicBakedModel {

    private static final MatrixStack EMPTY_STACK = new MatrixStack();

    protected final TextureAtlasSprite texture;

    public FenceBakedModel(TextureAtlasSprite texture) {
        this.texture = texture;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        Set<Connection.CompiledRenderData> data = extraData.getData(ProjectNublarModelData.CONNECTIONS);
        BakedQuadGenerator generator = new BakedQuadGenerator(this.texture);
        if(data != null) {
            for (Connection.CompiledRenderData renderData : data) {
                List<float[]> connectionData = renderData.getConnectionData();
                if(!connectionData.isEmpty()) {
                    if(renderData.isRenderSign()) {
                        //TODO: RENDER SIGN
                    }
                    for (float[] datum : connectionData) {
                        renderData(datum, generator);
                    }
                }
            }
        }

        return generator.poll();
    }

    private static void renderData(float[] data, BakedQuadGenerator generator) {
        RenderUtils.drawSpacedCube(EMPTY_STACK, generator, 1, 1, 1, 1, 0x00F000F0, OverlayTexture.NO_OVERLAY,
            data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
            data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17], data[18],
            data[19], data[20], data[21], data[22], data[23], data[24], data[25], data[26], data[27],
            data[28], data[29], data[30], data[31], data[32], data[33], data[34], data[35], data[36],
            data[37], data[38]);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.texture;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
