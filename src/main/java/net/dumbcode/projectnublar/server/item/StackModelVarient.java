package net.dumbcode.projectnublar.server.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
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
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface StackModelVarient<T> {

    /**
     * Get all the values {@code T} supports
     * @return a list of all the values that T supports.
     */
    List<T> getAllValues();

    /**
     * Get the unique name for the value T. As the method suggests,
     * this MUST be unique, otherwise values will mix and match
     * @param type the type
     * @return a unique name for {@code type}
     */
    String getUniqueNameFor(T type);

    /**
     * The reverse of {@link StackModelVarient#getUniqueNameFor}.
     * Takes in the unique name, and returns the {@code T} equivalent
     * @param string the unique name
     * @return the {@code T} that is represented by the name
     */
    T getValueFromName(String string);

    /**
     * Get the key used for nbt saving.
     * @return the key used for nbt. <b>MUST BE IMMUTABLE</b>
     */
    String getKey();

    /**
     * Returns a fallback value in case no value was found for a given name
     * @return a fallback value when a value is not found <b>MUST BE IMMUTABLE</b>
     */
    T getFallbackValue();

    /**
     * Get the default object for this stack. Must also bee immutable
     * @return a immutable, unchanging object
     */
    default Object getDefault() {
        return "__default";
    }

    /**
     * Creates a new itemstack from {@code T} value.
     * @param value the value
     * @return the new stack
     * @throws RuntimeException if an itemstack could not be created
     */
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

    /**
     * Put the value in the itemstacks nbt
     * @param stack the itemstack
     * @param value the value thats going to be put in the itemstack
     * @return {@code stack}, after the value has been put in
     */
    default ItemStack putValue(ItemStack stack, T value) {
        stack.getOrCreateSubCompound(ProjectNublar.MODID).setString(getKey(), getUniqueNameFor(value));
        return stack;
    }

    /**
     * Get the value from the {@code stack}.
     * @param stack the itemstack
     * @return the {@code T} value, or
     */
    default T getValue(ItemStack stack) {
        return this.getValueFromName(stack.getOrCreateSubCompound(ProjectNublar.MODID).getString(getKey()));
    }

    /**
     * Get all the itemstacks, only useing {@code T}. This list may be in a random order.
     * For a more ordered list, use {@link #getAllStacksOrdered()}
     * @return the list of itemstacks
     */
    default List<ItemStack> getAllStacks() {
        return getAllValues().stream().map(this::getItemStack).collect(Collectors.toList());
    }

    /**
     * get a list of all itemstacks, similar to {@link #getAllStacks()},
     * however the list is order, in alphabetical order of what {@link #getUniqueNameFor)} gives
     * @return
     */
    default List<ItemStack> getAllStacksOrdered() {//Rename to create tab thing
        List<String> keys = Lists.newArrayList(this.getKeySet());
        Collections.sort(keys);
        return keys.stream().map(this::getValueFromName).filter(this::canBeInCreativeTab).map(this::getItemStack).collect(Collectors.toList());
    }

    /**
     * Get a map of the model locations, in relation to whatever object you want.
     * Note that this map is per value, and should only be used for generating
     * models that are variants of the type. For example if you wanted an item
     * to have two different male and female textures (with the male and female
     * saving handled yourself), then you can return this map with size 2, one
     * entry for male, the other for female.
     * @param value the value
     * @return the map of mapped varients. Each Object should be able to get from {@link #getVarient(ItemStack)}
     */
    Map<Object, ResourceLocation> getModelResourceLocations(T value);

    /**
     * Gets the StackModelVarient from an itemstack
     * @param stack the itemstacl
     * @return the model varient, or null if it couldnt be found //TODO: dont make it return null. Maybe do an Optional ?
     */
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

    /**
     * Load and create the models that are needed for rendering. This method does
     * not need to be overriden, and should stay how it is.
     * @param map the {@link TextureMap} used to register textures to the texture atlas
     * @return the created map containing all the {@code T} values and all the varients
     */
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

    /**
     * Get the variant for the itemstack. This value will be used in the {@link #getModelResourceLocations(Object)}
     * to figure out which model is being overridden
     * @param stack the itemstack
     * @return the already mapped object
     */
    default Object getVarient(ItemStack stack) {
        return getDefault();
    }

    /**
     * Should the model override the already in place models (whether that be missing or not)
     * @param value the {@code T} value
     * @return if the custom model should be used
     */
    default boolean shouldOverrideModel(T value) {
        return true;
    }

    /**
     * Get a list of all {@code T} values. Should be mapped for {@link #getUniqueNameFor(Object)}
     * @return the list of strings
     */
    default List<String> getKeySet() {
        return this.getAllValues().stream().map(this::getUniqueNameFor).collect(Collectors.toList());
    }

    /**
     * Should the value be available in a creative tab
     * @param value the {@code T} type
     * @return if it should be in the creative tab
     */
    default boolean canBeInCreativeTab(T value) {
        return shouldOverrideModel(value);
    }
}