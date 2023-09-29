package net.dumbcode.projectnublar.client.model.fossil;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

public class FossilItemOverrideList extends ItemOverrideList {
	private final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;

	public FossilItemOverrideList(Function<RenderMaterial, TextureAtlasSprite> spriteGetterIn) {
		this.spriteGetter = spriteGetterIn;
	}

	/* Read NBT data from stack and choose what textures in use and merge them */
	@Override
	public IBakedModel resolve(@Nonnull IBakedModel model, ItemStack stack, ClientWorld worldIn, LivingEntity entityIn) {
		if (stack.getItem() instanceof FossilItem && model instanceof FossilItemBakedModel) {
			AtomicReference<FossilItemBakedModel> finalWandModel = new AtomicReference<>((FossilItemBakedModel) model);
			List<TextureAtlasSprite> sprites = new ArrayList<>();

			TextureAtlasSprite sprite = spriteGetter.apply(getFossil(stack));
			sprites.add(sprite);
			finalWandModel.set(finalWandModel.get().setSprites(sprites));
			return finalWandModel.get().getNewBakedItemModel();
		}
		return model;
	}

	private RenderMaterial getFossil(ItemStack stack) {
		Fossil fossil = FossilHandler.FOSSIL_REGISTRY.get().getValue(new ResourceLocation(Objects.requireNonNull(stack.getTagElement(ProjectNublar.MODID)).getString("fossil")));
        assert fossil != null;
		double dnaValue = 5; // TODO: get the fossil DNA value?
		ResourceLocation location = fossil.getTextureForDNAValue(dnaValue);
		ResourceLocation resourceLocation = new ResourceLocation(location.getNamespace(), "block/fossil/" + location.getPath() + "item/" + fossil.textureName.replace(" ", "_").toLowerCase());
        return new RenderMaterial(BLOCK_ATLAS, resourceLocation);
	}
}