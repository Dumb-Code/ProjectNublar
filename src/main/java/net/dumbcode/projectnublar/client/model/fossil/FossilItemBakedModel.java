package net.dumbcode.projectnublar.client.model.fossil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.dumbcode.dumblibrary.client.TransformingBakedQuadGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import org.joml.Vector3f;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.common.model.TransformationHelper;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

@SuppressWarnings("deprecation")
public class FossilItemBakedModel extends BakedItemModel implements ISelectiveResourceReloadListener {
	private TextureAtlasSprite baseSprite = null;
	private final List<TextureAtlasSprite> sprites;
	private final TransformationMatrix transform;
	/* Cache the result of quads, using a location combination */
	private static final Map<String, ImmutableList<BakedQuad>> cache = new HashMap<>();

	private static final float NORTH_Z = 7.496f / 16f;
	private static final float SOUTH_Z = 8.504f / 16f;

	public FossilItemBakedModel(Function<ResourceLocation, IUnbakedModel> modelGetter) {
		super(ImmutableList.of(), Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(MissingTextureSprite.getLocation()), getTransformMap(modelGetter), new FossilItemOverrideList(RenderMaterial::sprite), false, true);
		this.sprites = new ArrayList<>();
		this.transform = TransformationMatrix.identity();
	}

	private static ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> getTransformMap(Function<ResourceLocation, IUnbakedModel> modelGetter) {
		ImmutableMap.Builder<ItemCameraTransforms.TransformType, TransformationMatrix> builder = new ImmutableMap.Builder<>();

		ItemCameraTransforms parent = ((BlockModel) modelGetter.apply(new ResourceLocation("item/handheld"))).getTransforms();
		addTransform(builder, ItemCameraTransforms.TransformType.GUI, parent.gui);
		addTransform(builder, ItemCameraTransforms.TransformType.FIXED, parent.fixed);
		addTransform(builder, ItemCameraTransforms.TransformType.GROUND, parent.ground);
		addTransform(builder, ItemCameraTransforms.TransformType.HEAD, parent.head);
		addTransform(builder, ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, parent.firstPersonLeftHand);
		addTransform(builder, ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, parent.firstPersonRightHand);
		addTransform(builder, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, parent.thirdPersonLeftHand);
		addTransform(builder, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, parent.thirdPersonRightHand);
		return builder.build();
	}

	private static void addTransform(ImmutableMap.Builder<ItemCameraTransforms.TransformType, TransformationMatrix> builder, ItemCameraTransforms.TransformType type, ItemTransformVec3f vec) {
		builder.put(type, TransformationHelper.toTransformation(vec));
	}

	private FossilItemBakedModel(FossilItemBakedModel originalModel, List<TextureAtlasSprite> spritesIn) {
		super(ImmutableList.of(), originalModel.particle, originalModel.transforms, originalModel.overrides, originalModel.transform.isIdentity(), originalModel.isSideLit);

		this.baseSprite = originalModel.baseSprite;
		this.sprites = spritesIn;
		this.transform = originalModel.transform;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return new FossilItemOverrideList(RenderMaterial::sprite);
	}


	private ImmutableList<BakedQuad> genQuads() {
		String cacheKey = this.getCacheKeyString();

		/* Check if this sprite location combination is already baked or not  */
		if (FossilItemBakedModel.cache.containsKey(cacheKey)) {
			return FossilItemBakedModel.cache.get(cacheKey);
		}

		ImmutableList.Builder<BakedQuad> quads = ImmutableList.builder();
		List<TextureAtlasSprite> sprites = new ArrayList<>();

		if (this.baseSprite != null) {
			sprites.add(this.baseSprite);
		}
		sprites.addAll(this.sprites);

		if (!sprites.isEmpty()) {
			/* North & South Side */
			generateZQuads(quads);
			/* Up & Down-Side */
			generateTopDownQuads(quads);
			/* West & East Side */
			generateLeftRightQuads(quads);
		}

		ImmutableList<BakedQuad> returnQuads = quads.build();
		FossilItemBakedModel.cache.put(cacheKey, returnQuads);

		return returnQuads;
	}

	private void generateLeftRightQuads(ImmutableList.Builder<BakedQuad> quads) {
		for (int iy = 0; iy <= 15; iy++) {
			float yStart = (16 - (iy + 1)) / 16.0f;
			float yEnd = (16 - iy) / 16.0f;

			/* Scan from Left to Right, find the pixel not transparent, use that to build West quads */
			boolean isTransparent = true;
			for (int ix = 0; ix <= 15; ix++) {
				TextureAtlasSprite sprite = FossilItemBakedModel.findLastNotTransparent(ix, iy, sprites);
				if (sprite == null) {
					isTransparent = true;
					continue;
				}
				if (isTransparent) {
					quads.add(this.createQuad(
							new Vector3f(ix / 16.0f, yStart, FossilItemBakedModel.NORTH_Z)
							, new Vector3f(ix / 16.0f, yStart, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f(ix / 16.0f, yEnd, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f(ix / 16.0f, yEnd, FossilItemBakedModel.NORTH_Z)
							, ix, ix + 1, iy, iy + 1
							, sprite, Direction.WEST));

					isTransparent = false;
				}
			}
			/* Scan from Right to Left, find the pixel not transparent, use that to build East quads */
			isTransparent = true;
			for (int ix = 15; ix >= 0; ix--) {
				TextureAtlasSprite sprite = FossilItemBakedModel.findLastNotTransparent(ix, iy, sprites);
				if (sprite == null) {
					isTransparent = true;
					continue;
				}
				if (isTransparent) {
					quads.add(this.createQuad(
							new Vector3f((ix + 1) / 16.0f, yStart, FossilItemBakedModel.NORTH_Z)
							, new Vector3f((ix + 1) / 16.0f, yEnd, FossilItemBakedModel.NORTH_Z)
							, new Vector3f((ix + 1) / 16.0f, yEnd, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f((ix + 1) / 16.0f, yStart, FossilItemBakedModel.SOUTH_Z)
							, ix, ix + 1, iy, iy + 1
							, sprite, Direction.EAST));

					isTransparent = false;
				}
			}
		}
	}

	private void generateTopDownQuads(ImmutableList.Builder<BakedQuad> quads) {
		for (int ix = 0; ix <= 15; ix++) {
			float xStart = ix / 16.0f;
			float xEnd = (ix + 1) / 16.0f;

			/* Scan from Up to Bottom, find the pixel not transparent, use that to build Top quads */
			boolean isTransparent = true;
			for (int iy = 0; iy <= 15; iy++) {
				TextureAtlasSprite sprite = FossilItemBakedModel.findLastNotTransparent(ix, iy, sprites);
				if (sprite == null) {
					isTransparent = true;
					continue;
				}

				if (isTransparent) {
					quads.add(this.createQuad(
							new Vector3f(xStart, (16 - iy) / 16.0f, FossilItemBakedModel.NORTH_Z)
							, new Vector3f(xStart, (16 - iy) / 16.0f, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f(xEnd, (16 - iy) / 16.0f, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f(xEnd, (16 - iy) / 16.0f, FossilItemBakedModel.NORTH_Z)
							, ix, ix + 1, iy, iy + 1
							, sprite, Direction.UP));

					isTransparent = false;
				}
			}

			/* Scan from Bottom to Up, find the pixel not transparent, use that to build Down quads */
			isTransparent = true;
			for (int iy = 15; iy >= 0; iy--) {
				TextureAtlasSprite sprite = FossilItemBakedModel.findLastNotTransparent(ix, iy, sprites);
				if (sprite == null) {
					isTransparent = true;
					continue;
				}

				if (isTransparent) {
					quads.add(this.createQuad(
							new Vector3f(xStart, (16 - (iy + 1)) / 16.0f, FossilItemBakedModel.NORTH_Z)
							, new Vector3f(xEnd, (16 - (iy + 1)) / 16.0f, FossilItemBakedModel.NORTH_Z)
							, new Vector3f(xEnd, (16 - (iy + 1)) / 16.0f, FossilItemBakedModel.SOUTH_Z)
							, new Vector3f(xStart, (16 - (iy + 1)) / 16.0f, FossilItemBakedModel.SOUTH_Z)
							, ix, ix + 1, iy, iy + 1
							, sprite, Direction.DOWN));

					isTransparent = false;
				}
			}
		}
	}

	private void generateZQuads(ImmutableList.Builder<BakedQuad> quads) {
		for (int ix = 0; ix <= 15; ix++) {
			for (int iy = 0; iy <= 15; iy++) {
				/* Find the last pixel not transparent in sprites, use that to build North/South quads */
				TextureAtlasSprite sprite = FossilItemBakedModel.findLastNotTransparent(ix, iy, sprites);
				if (sprite == null) continue;

				float xStart = ix / 16.0f;
				float xEnd = (ix + 1) / 16.0f;

				float yStart = (16 - (iy + 1)) / 16.0f;
				float yEnd = (16 - iy) / 16.0f;

				BakedQuad a = this.createQuad(
						new Vector3f(xStart, yStart, FossilItemBakedModel.NORTH_Z)
						, new Vector3f(xStart, yEnd, FossilItemBakedModel.NORTH_Z)
						, new Vector3f(xEnd, yEnd, FossilItemBakedModel.NORTH_Z)
						, new Vector3f(xEnd, yStart, FossilItemBakedModel.NORTH_Z)
						, ix, ix + 1, iy, iy + 1
						, sprite, Direction.NORTH);

				BakedQuad b = this.createQuad(
						new Vector3f(xStart, yStart, FossilItemBakedModel.SOUTH_Z)
						, new Vector3f(xEnd, yStart, FossilItemBakedModel.SOUTH_Z)
						, new Vector3f(xEnd, yEnd, FossilItemBakedModel.SOUTH_Z)
						, new Vector3f(xStart, yEnd, FossilItemBakedModel.SOUTH_Z)
						, ix, ix + 1, iy, iy + 1
						, sprite, Direction.SOUTH);

				if (a != null) {
					quads.add(a);
				}
				if (b != null) {
					quads.add(b);
				}
			}
		}
	}

	/* Give four corner, generate a quad */
	private BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4
			, int xStart, int xEnd, int yStart, int yEnd, TextureAtlasSprite sprite
			, Direction orientation) {

		TransformingBakedQuadGenerator consumer = new TransformingBakedQuadGenerator(sprite, transform);

		FossilItemBakedModel.putVertex(consumer, v1, xStart, yEnd, sprite, orientation);
		FossilItemBakedModel.putVertex(consumer, v2, xStart, yStart, sprite, orientation);
		FossilItemBakedModel.putVertex(consumer, v3, xEnd, yStart, sprite, orientation);
		FossilItemBakedModel.putVertex(consumer, v4, xEnd, yEnd, sprite, orientation);

		List<BakedQuad> quads = consumer.poll();
		// we are only putting 4 vertices, so we don't need to worry about there being more quads but in case something goes horribly wrong, we throw
		if (quads.size() != 1) {
			throw new IllegalStateException("Model should not have put more than four vertices! Something is horribly wrong here!");
		}
		return quads.get(0);
	}

	/* Put data into the consumer */
	private static void putVertex(TransformingBakedQuadGenerator consumer, Vector3f vec, double u, double v, TextureAtlasSprite sprite, Direction orientation) {
		float fu = sprite.getU(u);
		float fv = sprite.getV(v);

		consumer.vertex(vec.x(), vec.y(), vec.z())
				.normal((float) orientation.getStepX(), (float) orientation.getStepY(), (float) orientation.getStepZ())
				.uv(fu, fv)
				.endVertex();
	}

	/* Find the last sprite not transparent in sprites with given position */
	@Nullable
	private static TextureAtlasSprite findLastNotTransparent(int x, int y, List<TextureAtlasSprite> sprites){
		for(int spriteIndex = sprites.size() - 1; spriteIndex >= 0; spriteIndex--){
			TextureAtlasSprite sprite = sprites.get(spriteIndex);
			if (sprite != null) {
				if (!sprite.isTransparent(0, x, y)) {
					return sprite;
				}
			}
		}
		return null;
	}

	/* Give a BakedItemModel base on data in this, can use directly to display */
	public BakedItemModel getNewBakedItemModel(){
		return new BakedItemModel(this.genQuads(), this.particle, transforms, this.overrides, false, this.isSideLit);
	}

	public FossilItemBakedModel setSprites(List<TextureAtlasSprite> spritesIn){
		return new FossilItemBakedModel(this, spritesIn);
	}

	/* Get a combination string of locations, used in cache's key */
	private String getCacheKeyString(){
		List<String> locations = new ArrayList<>();
		if(this.baseSprite != null)
			locations.add(this.baseSprite.getName().toString());

		for(TextureAtlasSprite sprite : this.sprites) {
			if (sprite != null) {
				locations.add(sprite.getName().toString());
			}
		}

        return String.join(",", locations);
	}

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		if (resourcePredicate.test(VanillaResourceType.MODELS)) {
			cache.clear();
		}
	}
}