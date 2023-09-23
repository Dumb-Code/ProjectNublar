package net.dumbcode.projectnublar.client.model.fossil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

//TODO: make transforms work
public class FossilBakedModel implements IBakedModel {
    String bottomTexture;
    String topTexture;
    String sideTexture;
    String particleTexture;
    String cracksTexture;
    String fossilTexture;

    IModelConfiguration owner;
    ModelBakery bakery;
    Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    IModelTransform modelTransform;
    ItemOverrideList overrides;
    ResourceLocation modelLocation;

    public FossilBakedModel(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation, ResourceLocation sideLocation, ResourceLocation topLocation, ResourceLocation bottomLocation, ResourceLocation particleLocation, ResourceLocation fossilLocation) {
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelTransform = modelTransform;
        this.overrides = overrides;
        this.modelLocation = modelLocation;
        this.sideTexture = sideLocation.toString();
        this.topTexture = topLocation.toString();
        this.bottomTexture = bottomLocation.toString();
        this.particleTexture = particleLocation.toString();
        this.fossilTexture = fossilLocation.toString();
    }

    //TODO: this method is disgusting
    public String getJsonString() {
        if (cracksTexture == null) {
            return String.join("\n", "{",
                    "\"parent\": \"block/block\",",
                    "\"textures\": {",
                    "    \"particle\": \"" + particleTexture + "\",",
                    "    \"bottom\": \"" + bottomTexture + "\",",
                    "    \"top\": \"" + topTexture + "\",",
                    "    \"side\": \"" + sideTexture + "\",",
                    "    \"overlay\": \"" + fossilTexture + "\"",
                    "  },",
                    "\"elements\": [",
                    "    {",
                    "      \"from\": [ 0, 0, 0 ],",
                    "      \"to\": [ 16, 16, 16 ],",
                    "      \"faces\": {",
                    "        \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#bottom\", \"cullface\": \"down\" },",
                    "        \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#top\",    \"cullface\": \"up\" },",
                    "        \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"north\" },",
                    "        \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"south\" },",
                    "        \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"west\" },",
                    "        \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"east\" }",
                    "      }",
                    "    },",
                    "    {",
                    "      \"from\": [ 0, 0, 0 ],",
                    "      \"to\": [ 16, 16, 16 ],",
                    "      \"faces\": {",
                    "        \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"down\" },",
                    "        \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"up\" },",
                    "        \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"north\" },",
                    "        \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"south\" },",
                    "        \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"west\" },",
                    "        \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"east\" }",
                    "        }",
                    "    }",
                    "  ]",
                    "}"
            );
        } else {
            return String.join("\n", "{",
                    "\"parent\": \"block/block\",",
                    "\"textures\": {",
                    "    \"particle\": \"" + particleTexture + "\",",
                    "    \"bottom\": \"" + bottomTexture + "\",",
                    "    \"top\": \"" + topTexture + "\",",
                    "    \"side\": \"" + sideTexture + "\",",
                    "    \"cracks\": \"" + cracksTexture + "\",",
                    "    \"overlay\": \"" + fossilTexture + "\"",
                    "  },",
                    "\"elements\": [",
                    "    {",
                    "      \"from\": [ 0, 0, 0 ],",
                    "      \"to\": [ 16, 16, 16 ],",
                    "      \"faces\": {",
                    "        \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#bottom\", \"cullface\": \"down\" },",
                    "        \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#top\",    \"cullface\": \"up\" },",
                    "        \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"north\" },",
                    "        \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"south\" },",
                    "        \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"west\" },",
                    "        \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#side\",   \"cullface\": \"east\" }",
                    "      }",
                    "    },",
                    "    {",
                    "      \"from\": [ 0, 0, 0 ],",
                    "      \"to\": [ 16, 16, 16 ],",
                    "      \"faces\": {",
                    "        \"down\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"down\" },",
                    "        \"up\":    { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"up\" },",
                    "        \"north\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"north\" },",
                    "        \"south\": { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"south\" },",
                    "        \"west\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"west\" },",
                    "        \"east\":  { \"uv\": [ 0, 0, 16, 16 ], \"texture\": \"#overlay\",   \"cullface\": \"east\" }",
                    "        }",
                    "    },",
                    "    {",
                    "      \"from\": [0, 0, 0],",
                    "      \"to\": [16, 16, 16],",
                    "      \"faces\": {",
                    "        \"north\": { \"uv\": [0, 0, 16, 16], \"texture\": \"#cracks\", \"cullface\": \"north\" },",
                    "        \"east\": { \"uv\": [0, 0, 16, 16], \"texture\": \"#cracks\", \"cullface\": \"east\" },",
                    "        \"south\": { \"uv\": [0, 0, 16, 16], \"texture\": \"#cracks\", \"cullface\": \"south\" },",
                    "        \"west\": { \"uv\": [0, 0, 16, 16], \"texture\": \"#cracks\", \"cullface\": \"west\" },",
                    "        \"up\": { \"uv\": [16, 16, 0, 0], \"texture\": \"#cracks\", \"cullface\": \"up\" },",
                    "        \"down\": { \"uve\": [16, 0, 0, 16], \"texture\": \"#cracks\", \"cullface\": \"down\" }",
                    "      }",
                    "    }",
                    "  ]",
                    "}"
            );
        }
    }

    public static final Map<String, List<BakedQuad>> cache = new HashMap<>();
    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
//        this.cracksTexture = extraData.getData(FossilBlockEntity.CRACKS_TEX);
        String cacheKey = getCacheKey(side);
        if (!cache.containsKey(cacheKey)) {
            Gson gson = (new GsonBuilder()).registerTypeAdapter(BlockModel.class, new BlockModel.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer()).create();
            BlockModel model = gson.fromJson(getJsonString(), BlockModel.class);
//            ((BlockModelAccessor) model).setTransforms(ItemCameraTransforms.);
            Supplier<List<BakedQuad>> quads = () -> model.bake(bakery, model, spriteGetter, modelTransform, modelLocation, true).getQuads(state, side, rand, extraData);
            cache.put(cacheKey, quads.get());
            return quads.get();
        }
        return cache.get(cacheKey);
    }


    @Nonnull
    @Override
    public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
        if(tileData == EmptyModelData.INSTANCE) {
            TileEntity entity = world.getBlockEntity(pos);
            if(entity != null) {
                tileData = entity.getModelData();
            }
        }
        return tileData;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, Random pRand) {
        return getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(BLOCK_ATLAS).apply(new ResourceLocation(particleTexture));
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    public String getCacheKey(Direction direction) {
        return String.join(", ",
                "Direction:" + direction,
                "TopTexture:" + topTexture,
                "BottomTexture:" + bottomTexture,
                "SideTexture:" + sideTexture,
                "ParticleTexture:" + particleTexture,
                "CracksTexture:" + cracksTexture,
                "FossilTexture:" + fossilTexture
                );
    }
}
