package net.dumbcode.projectnublar.client.model.fossil;

import net.dumbcode.projectnublar.client.model.ModelUtils;
import net.dumbcode.projectnublar.client.render.model.ProjectNublarModelHandler;
import net.dumbcode.projectnublar.server.block.entity.FossilBlockEntity;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.FossilTier;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

public class FossilBakedModel implements IDynamicBakedModel {

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        BlockState baseState = getOrDefault(extraData, FossilBlockEntity.STONE_TYPE, StoneTypeHandler.GRANITE.get()).baseState.get();
        IBakedModel baseModel = Minecraft.getInstance().getModelManager().getModel(BlockModelShapes.stateToModelLocation(baseState));

        List<BakedQuad> solidQuads = baseModel.getQuads(state, side, rand, extraData);

        // The fossil model can be rendered in both the solid, and translucent render layers.
        // If, it's the solid render layer, render the block as normal.
        // Otherwise, render the retextured quads.
        if (MinecraftForgeClient.getRenderLayer() == RenderType.solid()) {
            return solidQuads;
        }


        Fossil fossil = getOrDefault(extraData, FossilBlockEntity.FOSSIL, FossilHandler.AMMONITE.get());
        FossilTier tier = getOrDefault(extraData, FossilBlockEntity.TIER, FossilTier.WEATHERED);

        // Get the texture
        ResourceLocation texture = fossil.getTextureForDNAValue(tier.getDnaValue());
        ResourceLocation finalTexture = new ResourceLocation(texture.getNamespace(), "block/fossil/" + texture.getPath() + "overlay/" + fossil.textureName);
        TextureAtlasSprite overlay = Minecraft.getInstance().getModelManager().getAtlas(BLOCK_ATLAS).getSprite(finalTexture);

        TextureAtlasSprite crack = this.getCrackTexture(tier);

        List<BakedQuad> out = new ArrayList<>();
        for (BakedQuad quad : solidQuads) {
            out.add(ModelUtils.retextureQuad(quad, overlay, 0.001F));
            if (crack != null) {
                out.add(ModelUtils.retextureQuad(quad, crack, 0.002F));
            }
        }
        return out;
    }

    private TextureAtlasSprite getCrackTexture(FossilTier tier) {
        switch (tier.getCrackLevel()) {
            case 0:
                return null;
            case 1:
                return ProjectNublarModelHandler.fossilCrackLow;
            case 2:
                return ProjectNublarModelHandler.fossilCrackMedium;
            default:
                return ProjectNublarModelHandler.fossilCrackFull;
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        return Objects.requireNonNull(world.getBlockEntity(pos)).getModelData();
    }

    public static <T> T getOrDefault(IModelData data, ModelProperty<T> property, T fallback) {
        if (data.hasProperty(property)) {
            return data.getData(property);
        }
        return fallback;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        BlockState baseState = getOrDefault(data, FossilBlockEntity.STONE_TYPE, StoneTypeHandler.GRANITE.get()).baseState.get();
        IBakedModel baseModel = Minecraft.getInstance().getModelManager().getModel(BlockModelShapes.stateToModelLocation(baseState));
        return baseModel.getParticleTexture(data);
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
