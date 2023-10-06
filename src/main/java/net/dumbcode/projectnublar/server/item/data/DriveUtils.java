package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import lombok.Value;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class DriveUtils {

    public static boolean canAdd(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return false;
        }
        DriveInformation info = (DriveInformation) inItem.getItem();
        String key = info.getKey(inItem);
        if(key.isEmpty()) {
            return false;
        }
        return getAmount(drive, key, info.getAnimalVariant(inItem)) < 100;
    }

    public static List<DriveEntry> getAll(ItemStack drive) {
        List<DriveEntry> out = Lists.newArrayList();
        ListNBT nbt = drive.getOrCreateTagElement(ProjectNublar.MODID).getList("drive_information", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT tag = nbt.getCompound(i);
            out.add(new DriveEntry(tag.getString("drive_key"), tag.getString("translation_key"), tag.contains("animal_variant") ? tag.getString("animal_variant") : null, tag.getInt("amount"), DriveType.values()[tag.getInt("drive_type") % DriveType.values().length]));
        }

        return out;
    }


    public static int getAmount(ItemStack drive, String key, String variant) {
        for (DriveEntry entry : getAll(drive)) {
            if(entry.getKey().equals(key) && Objects.equals(variant, entry.getVariant())) {
                return entry.getAmount();
            }
        }
        return 0;
    }

    public static DriveType getType(ItemStack drive, String key, String variant) {
        for (DriveEntry entry : getAll(drive)) {
            if(entry.getKey().equals(key) && Objects.equals(variant, entry.getVariant())) {
                return entry.getDriveType();
            }
        }
        return DriveType.OTHER; //??
    }

    public static void addItemToDrive(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();
        ListNBT list = drive.getOrCreateTagElement(ProjectNublar.MODID).getList("drive_information", Constants.NBT.TAG_COMPOUND);
        String key = info.getKey(inItem);
        String variant = info.getAnimalVariant(inItem);
        if(key.isEmpty()) {
            return;
        }

        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT nbt = list.getCompound(i);
            if(nbt.getString("drive_key").equals(key) && (nbt.contains("animal_variant") ? nbt.getString("animal_variant").equals(variant) : variant == null)) {
                index = i;
            }
        }
        CompoundNBT inner = list.getCompound(index);
        int current = inner.getInt("amount");
        if(current >= 100) {
            return;
        }
        int result = info.getSize(inItem);
        inner.putString("drive_key", key);
        inner.putInt("amount", Mth.clamp(current + result, 0, 100));
        inner.putString("translation_key", info.getDriveTranslationKey(inItem));
        if(variant != null) {
            inner.putString("animal_variant", variant);
        }
        inner.putInt("drive_type", info.getDriveType(inItem).ordinal());

        if(index == -1) {
            list.add(inner);
        } else {
            list.set(index, inner);
        }

        drive.getOrCreateTagElement(ProjectNublar.MODID).put("drive_information", list);
    }

    public static List<IsolatedGeneEntry> getAllIsolatedGenes(ItemStack drive) {
        Map<GeneticType<?, ?>, Double> counter = new HashMap<>();
        Map<EntityType<?>, Integer> entityAmountMap = new HashMap<>();

        Map<DyeColor, Integer> tropicalFishColours = new HashMap<>(); //For the colours;

        for (DriveEntry entry : getAll(drive)) {
            entry.getEntity().ifPresent(e -> {
                //Get the maximum amount per animal. If the entity has 50 in the red variant, and 65 in the blue variant, entityAmountMap will contain 65.
                entityAmountMap.compute(e, (t, amount) -> Math.max(amount != null ? amount : 0, entry.getAmount()));

                if(e == EntityType.TROPICAL_FISH) {
                    DyeColor dyeColor = DyeColor.byName(entry.getVariant(), null);
                    if(dyeColor != null && dyeColor != DyeColor.BLACK) {
                        tropicalFishColours.put(dyeColor, entry.getAmount());
                    }
                }
            });
        }

        entityAmountMap.forEach((entityType, entityAmount) -> {
            for (EntityGeneticRegistry.Entry<?, ?> registryEntry : EntityGeneticRegistry.INSTANCE.gatherEntry(entityType, null)) {
                counter.compute(registryEntry.getType(), (type, amount) -> (amount == null ? 0 : amount) + entityAmount / 100D);
            }
        });

        List<IsolatedGeneEntry> entries = new ArrayList<>();
        counter.forEach((type, amount) -> {
            List<EntityGeneticRegistry.IsolatePart> isolate = EntityGeneticRegistry.INSTANCE.getEntriesToIsolate(type);
            entries.add(new IsolatedGeneEntry(type, amount / isolate.size(),
                isolate.stream()
                    .map(p -> Pair.of(new TranslationTextComponent(p.getEntityType().getDescriptionId()), entityAmountMap.getOrDefault(p.getEntityType(), 0) / 100D))
                    .collect(Collectors.toList())
            ));
        });

        if(!tropicalFishColours.isEmpty()) {
            entries.add(new IsolatedGeneEntry(
                GeneticTypes.OVERALL_TINT.get(),
                tropicalFishColours.values().stream().mapToDouble(Integer::doubleValue).sum() / (DyeColor.values().length-1) / 100D,
                Arrays.stream(DyeColor.values())
                    .filter(c -> c != DyeColor.BLACK)
                    .map(d -> Pair.of(DriveUtils.getTranslation(EntityType.TROPICAL_FISH.getDescriptionId(), d.getName()), tropicalFishColours.getOrDefault(d, 0) / 100D))
                    .collect(Collectors.toList())
            ));
        }

        return entries;
    }


    public static TranslationTextComponent getTranslation(String name, String variant) {
        TranslationTextComponent component = new TranslationTextComponent(name);
        if(variant != null) {
            return ProjectNublar.translate("entity.genetics.variant." + variant.toLowerCase(Locale.ROOT), component);
        } else {
            return component;
        }
    }

    public interface DriveInformation {
        int getSize(ItemStack stack);
        String getKey(ItemStack stack);
        String getDriveTranslationKey(ItemStack stack);
        default String getAnimalVariant(ItemStack stack) {
            return null;
        }
        default DriveType getDriveType(ItemStack stack) {
            return DriveType.OTHER;
        }
        default boolean hasInformation(ItemStack stack) {
            return true;
        }
        default ItemStack getOutItem(ItemStack stack) {
            return ItemStack.EMPTY;
        }
    }

    @Value
    public static class DriveEntry {
        String key;
        String name;
        String variant;
        int amount;
        DriveType driveType;

        public MutableComponent getTranslation() {
            return DriveUtils.getTranslation(this.name, this.variant);
        }

        public Optional<EntityType<?>> getEntity() {
            if(this.driveType == DriveType.OTHER) {
                ResourceLocation location = new ResourceLocation(this.getKey());
                if (ForgeRegistries.ENTITIES.containsKey(location)) {
                    return Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(location));
                }
            }
            return Optional.empty();
        }
    }

    @Value
    public static class IsolatedGeneEntry {
        GeneticType<?, ?> geneticType;
        double progress;
        List<Pair<MutableComponent, Double>> parts;
    }

    public enum DriveType {
        DINOSAUR,
        OTHER
    }

}
