package net.dumbcode.projectnublar.server.runtimepack.generator.json.lang;

import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>The <b>language file</b> determines how Minecraft will translate strings according to their language settings.</p>
 */
@SuppressWarnings("unused")
public class JLang extends HashMap<String, String> implements Cloneable {

  public JLang() {
  }

  /**
   * Simply "upgrades" a simple string map to this object.
   *
   * @param map The string map.
   */
  public JLang(Map<? extends String, ? extends String> map) {
    this();
    putAll(map);
  }

  /**
   * @see #JLang()  JLang
   * @deprecated Please directly use the constructor method {@link #JLang()}.
   */
  @Deprecated
  public static JLang lang() {
    return new JLang();
  }

  /**
   * Adds a custom entry to the lang file. (deprecated: renamed to a more intuitive name)
   *
   * @param in  the translation string
   * @param out the in-game name of the object
   * @return the file with the new entry.
   * @deprecated use {@link #entry(String, String)} instead.
   */
  @Deprecated
  public JLang translate(String in, String out) {
    put(in, out);
    return this;
  }

  /**
   * @deprecated Ambiguous name and parameter name. Please use {@link #registryEntry(IForgeRegistry, String, IForgeRegistryEntry, String)}.
   */
  @Deprecated
  private <T> JLang object(Registry<T> registry, String str, T t, String name) {
    return this.object(str,
        Objects.requireNonNull(registry.getKey(t), "register your item before calling this"),
        name);
  }

  /**
   * Add a registry entry to this instance.
   *
   * @deprecated Ambiguous name and it does not use {@link Util#makeDescriptionId}. Please use {@link #registryEntry(String, ResourceLocation, String)}.
   */
  @Deprecated
  private JLang object(String type, ResourceLocation identifier, String translation) {
    put(type + '.' + identifier.getNamespace() + '.' + identifier.getPath(), translation);
    return this;
  }

  /**
   * Add a registry entry to this instance, using {@link Util#makeDescriptionId(String, ResourceLocation)}.
   *
   * @param type        The name specification of the registry type. For example, {@code "block"}.
   * @param identifier  The identifier of the object. For example, {@code new ResourceLocation("minecraft", "stone")}.
   * @param translation The translated name, such as {@code "Stone"}.
   * @author SolidBlock
   */

  public JLang registryEntry(String type, ResourceLocation identifier, String translation) {
    put(Util.makeDescriptionId(type, identifier), translation);
    return this;
  }

  /**
   * Add a registry entry to this instance, using {@link Registry#getId(Object)}.
   */

  public <T extends IForgeRegistryEntry<T>> JLang registryEntry(IForgeRegistry<T> registry, String type, T t, String translation) {
    return registryEntry(type, Preconditions.checkNotNull(registry.getKey(t), "Please register it first!"), translation);
  }


  /**
   * Add a language entry to this language file.
   *
   * @param entry       The key. For most registrable contents, it is <code><i>type</i>.<i>namespace</i>.<i>path</i></code>, for example, <code>block.minecraft.stone</code>. Customized keys are also OK.
   * @param translation The translated words.
   * @return The instance itself, making it possible to chain-call.
   */

  public JLang entry(String entry, String translation) {
    put(entry, translation);
    return this;
  }

  /**
   * Adds a translation key for an item, respects {@link Item#getDescriptionId()}. Please ensure that the item has been registered.
   */

  public JLang itemRespect(Item item, String translation) {
    put(item.getDescriptionId(), translation);
    return this;
  }

  /**
   * Adds a translation key for an item stack (usually identical to that item), respected {@link ItemStack#getDescriptionId()}. Typically, you should ensure that the item has been registered.
   */

  public JLang item(ItemStack stack, String translation) {
    put(stack.getDescriptionId(), translation);
    return this;
  }

  /**
   * Adds a translation key for an item, using simple {@link Registry#getId(Object)}. Please ensure that the item has been registered.
   *
   * @see #itemRespect(Item, String)
   */

  @Deprecated
  public JLang item(Item item, String translation) {
    return this.registryEntry(ForgeRegistries.ITEMS, "item", item, translation);
  }

  /**
   * Adds a translation key for a block, respects {@link Block#getDescriptionId()}. Please ensure that the block has been registered.
   */

  public JLang blockRespect(Block block, String translation) {
    put(block.getDescriptionId(), translation);
    return this;
  }

  /**
   * Adds a translation key for a block, using simple {@link Registry#getId(Object)}. Please ensure that the block has been registered.
   *
   * @see #blockRespect(Block, String)
   */

  @Deprecated
  public JLang block(Block block, String translation) {
    return this.registryEntry(ForgeRegistries.BLOCKS, "block", block, translation);
  }

  /**
   * Adds a translation key for a fluid, using simple {@link Registry#getId(Object)}. Please ensure that the fluid has been registered.
   */

  public JLang fluid(Fluid fluid, String translation) {
    return this.registryEntry(ForgeRegistries.FLUIDS, "fluid", fluid, translation);
  }

  /**
   * Adds a translation key for an entity type, respects {@link EntityType#getDescriptionId()}. Please ensure that the entity has been registered.
   */

  public JLang entityRespect(EntityType<?> type, String translation) {
    put(type.getDescriptionId(), translation);
    return this;
  }

  /**
   * @see JLang#entityRespect(EntityType, String)
   */
  @Deprecated

  public JLang entity(EntityType<?> type, String translation) {
    return this.object(Registry.ENTITY_TYPE, "entity_type", type, translation);
  }

  /**
   * Adds a translation key for an enchantment, respects {@link Enchantment#getDescriptionId()}.
   */

  public JLang enchantmentRespect(Enchantment enchantment, String translation) {
    put(enchantment.getDescriptionId(), translation);
    return this;
  }

  /**
   * Adds a translation key for an enchantment, simply using {@link Registry#getId(Object)}.
   *
   * @see #enchantmentRespect(Enchantment, String)
   */
  @Deprecated

  public JLang enchantment(Enchantment enchantment, String translation) {
    return this.object(Registry.ENCHANTMENT, "enchantment", enchantment, translation);
  }

  /**
   * Add an item entry with the identifier specified.
   */

  @Deprecated
  public JLang item(ResourceLocation item, String translation) {
    return this.registryEntry("item", item, translation);
  }

  /**
   * Add a block entry with the identifier specified.
   *
   * @see #blockRespect(Block, String)
   */

  @Deprecated
  public JLang block(ResourceLocation block, String translation) {
    return this.registryEntry("block", block, translation);
  }

  /**
   * Add a fluid entry with the identifier specified.
   *
   * @see #fluid(Fluid, String)
   */

  public JLang fluid(ResourceLocation id, String translation) {
    return this.registryEntry("fluid", id, translation);
  }

  /**
   * Add an entity-type entry with the identifier specified.
   *
   * @see #entityRespect(EntityType, String)
   */

  public JLang entity(ResourceLocation id, String translation) {
    return this.registryEntry("entity_type", id, translation);
  }

  /**
   * Add an enchantment entry with the identifier specified.
   *
   * @see #enchantmentRespect(Enchantment, String)
   */

  public JLang enchantment(ResourceLocation id, String translation) {
    return this.registryEntry("enchantment", id, translation);
  }

  /**
   * Add an item-group entry with the identifier specified.
   *
   * @param id          The identifier of the item group.
   * @param translation The translated name of the item group.
   */

  public JLang itemGroup(ResourceLocation id, String translation) {
    return this.registryEntry("itemGroup", id, translation);
  }

  /**
   * Add a sound event with the identifier specified.
   */

  public JLang sound(ResourceLocation id, String translation) {
    return this.registryEntry("sound_event", id, translation);
  }

  /**
   * Add a mob effect with the identifier specified.
   */

  public JLang status(ResourceLocation id, String translation) {
    return this.registryEntry("mob_effect", id, translation);
  }

  /**
   * Like {@link JLang#allPotion}, but it adds in the prefixes automatically. This only applies for English language.
   *
   * @deprecated Ambiguous translation keys and English-only potion names.
   */
  @Deprecated

  public JLang allPotionOf(ResourceLocation id, String effectTranslation) {
    this.allPotion(id,
        "Potion of " + effectTranslation,
        "Splash Potion of " + effectTranslation,
        "Lingering Potion of " + effectTranslation,
        "Tipped Arrow of " + effectTranslation);
    return this;
  }

  /**
   * Add translation entries for drinkable potion, splash potion, lingering potion and tipped arrow.
   *
   * @deprecated Ambiguous translation keys.
   */
  @Deprecated
  public JLang allPotion(ResourceLocation id,
                         String drinkablePotionName,
                         String splashPotionName,
                         String lingeringPotionName,
                         String tippedArrowName) {
    return this.drinkablePotion(id, drinkablePotionName).splashPotion(id, splashPotionName)
        .lingeringPotion(id, lingeringPotionName).tippedArrow(id, tippedArrowName);
  }

  /**
   * @deprecated Ambiguous translation key.
   */
  @Deprecated

  public JLang tippedArrow(ResourceLocation id, String translation) {
    put("item.minecraft.tipped_arrow.effect." + id.getPath(), translation);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key.
   */
  @Deprecated

  public JLang lingeringPotion(ResourceLocation id, String name) {
    put("item.minecraft.lingering_potion.effect." + id.getPath(), name);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key.
   */
  @Deprecated

  public JLang splashPotion(ResourceLocation id, String name) {
    put("item.minecraft.splash_potion.effect." + id.getPath(), name);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key and English-only potion names.
   */
  @Deprecated

  public JLang drinkablePotion(ResourceLocation id, String name) {
    put("item.minecraft.potion.effect." + id.getPath(), "Potion of " + name);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key and English-only potion names.
   * Like {@link JLang#drinkablePotion}, but it adds in the "Potion of" automatically.
   */
  @Deprecated

  public JLang drinkablePotionOf(ResourceLocation id, String effectName) {
    put("item.minecraft.potion.effect." + id.getPath(), "Potion of " + effectName);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key and English-only potion names.
   * <p>
   * Like {@link JLang#splashPotion}, but it adds in the "Splash Potion of" automatically.
   */
  @Deprecated

  public JLang splashPotionOf(ResourceLocation id, String effectName) {
    put("item.minecraft.splash_potion.effect." + id.getPath(), "Splash Potion of " + effectName);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key and English-only potion names.
   * <p>
   * Like {@link JLang#lingeringPotion}, but it adds in the "Lingering Potion of" automatically.
   */
  @Deprecated

  public JLang lingeringPotionOf(ResourceLocation id, String effectName) {
    put("item.minecraft.lingering_potion.effect." + id.getPath(), "Lingering Potion of " + effectName);
    return this;
  }

  /**
   * @deprecated Ambiguous translation key and English-only potion names.
   * <p>
   * Like {@link JLang#tippedArrow}, but it adds in the "Tipped Arrow of" automatically.
   */
  @Deprecated

  public JLang tippedArrowOf(ResourceLocation id, String effectName) {
    put("item.minecraft.tipped_arrow.effect." + id.getPath(), "Tipped Arrow of " + effectName);
    return this;
  }

  /**
   * Add a biome entry to this instance with the identifier specified.
   *
   * @param id          The identifier of the biome.
   * @param translation The translated name of the biome.
   */
  public JLang biome(ResourceLocation id, String translation) {
    return this.registryEntry("biome", id, translation);
  }

  /**
   * @deprecated Useless method. Kept for only compatibility.
   */
  @Deprecated
  public Map<String, String> getLang() {
    return this;
  }

  @Override
  public JLang clone() {
    return (JLang) super.clone();
  }
}
