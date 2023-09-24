package net.dumbcode.projectnublar.client.model.fossil;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.client.model.ModelUtils;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilBlockItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

public class FossilItemBakedModel implements IDynamicBakedModel {
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return Collections.emptyList();
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
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return StackAwareItemOverrides.INSTANCE;
    }

    private static class StackAwareItemOverrides extends ItemOverrideList {

        public static final StackAwareItemOverrides INSTANCE = new StackAwareItemOverrides();

        @Nullable
        @Override
        public IBakedModel resolve(IBakedModel model, ItemStack itemStack, @Nullable ClientWorld p_239290_3_, @Nullable LivingEntity p_239290_4_) {
            return new StackAwareModel(FossilBlockItem.getStoneType(itemStack), FossilBlockItem.getFossil(itemStack));
        }
    }

    private static class StackAwareModel extends FossilItemBakedModel {
        private final Fossil fossil;

        private final IBakedModel base;

        private StackAwareModel(StoneType type, Fossil fossil) {
            this.fossil = fossil;
            this.base = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(type.baseState.get().getBlock()));
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
            List<BakedQuad> quads = base.getQuads(state, side, rand, extraData);
            List<BakedQuad> out = new ArrayList<>(quads);

            TextureAtlasSprite overlay = Minecraft.getInstance().getModelManager().getAtlas(BLOCK_ATLAS).getSprite(fossil.texture);

            for (BakedQuad quad : quads) {
                out.add(ModelUtils.retextureQuad(quad, overlay, 0.001F));
            }

            return out;
        }

        @Override
        public ItemCameraTransforms getTransforms() {
            return this.base.getTransforms();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return base.getParticleIcon();
        }
    }
}
