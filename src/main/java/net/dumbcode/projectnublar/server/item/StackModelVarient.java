package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface StackModelVarient<T extends IForgeRegistryEntry.Impl<T>> {

    IForgeRegistry<T> getRegistry();

    default List<T> getAllValues() {
        return Lists.newArrayList(getRegistry().getValuesCollection());
    }

    default String getNameFor(T type) {
        return type.getRegistryName().toString();
    }

    default T getValueFromName(String string) {
        return getRegistry().getValue(new ResourceLocation(string));
    }

    String getKey();

    default Object getDefault() {
        return "__default";
    }

    default ItemStack getItemStack(T value) {
        ItemStack stack;
        if(this instanceof Item) {
            stack = new ItemStack((Item)this);
        } else if(this instanceof Block) {
            stack = new ItemStack((Block)this);
        } else {
            throw new RuntimeException("DinosaurProvider was applied on a non item/block");
        }
        return this.putValue(stack, value);
    }

    default ItemStack putValue(ItemStack stack, T value) {
        stack.getOrCreateSubCompound("jurassicraft").setString(getKey(), getNameFor(value));
        return stack;
    }

    default T getValue(ItemStack stack) {
        return this.getValueFromName(stack.getOrCreateSubCompound("jurassicraft").getString(getKey()));
    }

    default List<ItemStack> getAllStacks() {
        return getAllValues().stream().map(this::getItemStack).collect(Collectors.toList());
    }

    default List<ItemStack> getAllStacksOrdered() {//Rename to create tab thing
        List<String> keys = Lists.newArrayList(this.getKeySet());
        Collections.sort(keys);
        return keys.stream().map(this::getValueFromName).filter(this::canBeInCreativeTab).map(this::getItemStack).collect(Collectors.toList());
    }

    default Map<Object, ResourceLocation> getModelResourceLocations(T value) {
        ResourceLocation res = ((IForgeRegistryEntry.Impl)this).getRegistryName(); //A bit of a hack, but whatever
        ResourceLocation reg = value.getRegistryName();
        if(reg == null) {
            throw new NullPointerException("Registry name was null");
        }
        Map<Object, ResourceLocation> ret = Maps.newHashMap();
        ret.put(getDefault(), new ResourceLocation(reg.getResourceDomain(), getFolderLocation(res) + "/" + reg.getResourcePath()));
        return ret;
    }

    default String getFolderLocation(ResourceLocation res) {
        return "item/" + res.getResourcePath();
    }

    @Nullable
    static StackModelVarient getFromStack(ItemStack stack) {
        Item item = stack.getItem();
        if(item instanceof StackModelVarient) {
            return (StackModelVarient)item;
        } else if((item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof StackModelVarient)) {
            return (StackModelVarient)((ItemBlock)item).getBlock();
        } else {
            return null;
        }
    }

    default Map<T, Map<Object, IBakedModel>> produceMap(TextureMap map) {
        Map<T, Map<Object, IBakedModel>> modelMap = Maps.newHashMap();
        this.getAllValues().stream().filter(this::shouldOverrideModel).forEach(t ->
                this.getModelResourceLocations(t).forEach((s, location) -> {
                    try {
                        modelMap.computeIfAbsent(t, dino -> Maps.newHashMap()).put(s, ModelLoaderRegistry.getModel(location).bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, map::registerSprite));
                    } catch (Exception e) {
                        ProjectNublar.getLogger().fatal("Error loading model", e);
                    }
                })
        );
        return modelMap;
    }

    default Object getVarient(ItemStack stack) {
        return getDefault();
    }

    default boolean shouldOverrideModel(T value) {
        return true;
    }

    default List<String> getKeySet() {
        return this.getAllValues().stream().map(this::getNameFor).collect(Collectors.toList());
    }

    default boolean canBeInCreativeTab(T value) {
        return shouldOverrideModel(value);
    }
}