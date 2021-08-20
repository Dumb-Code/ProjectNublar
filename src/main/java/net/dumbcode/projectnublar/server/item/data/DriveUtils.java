package net.dumbcode.projectnublar.server.item.data;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import lombok.Value;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dna.GeneticHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
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

    public static ItemStack addItemToDrive(ItemStack drive, ItemStack inItem) {
        if (!(inItem.getItem() instanceof DriveInformation)) {
            return drive;
        }

        DriveInformation info = (DriveInformation) inItem.getItem();
        ItemStack out = drive.copy();
        ListNBT list = out.getOrCreateTagElement(ProjectNublar.MODID).getList("drive_information", Constants.NBT.TAG_COMPOUND);
        String key = info.getKey(inItem);
        String variant = info.getAnimalVariant(inItem);
        if(key.isEmpty()) {
            return drive;
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
            return drive;
        }
        int result = info.getSize(inItem);
        inner.putString("drive_key", key);
        inner.putInt("amount", MathHelper.clamp(current + result, 0, 100));
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

        out.getOrCreateTagElement(ProjectNublar.MODID).put("drive_information", list);
        return out;
    }

    public static List<IsolatedGeneEntry> getAllIsolatedGenes(ItemStack drive) {
        Map<GeneticType<?, ?>, Double> counter = new HashMap<>();
        Map<EntityType<?>, Integer> entityAmountMap = new HashMap<>();

        Map<DyeColor, Integer> tropicalFishColours = new HashMap<>(); //For the colours;

        for (DriveEntry entry : getAll(drive)) {
            entry.getEntity().ifPresent(e -> {
                entityAmountMap.compute(e, (t, amount) -> Math.max(amount != null ? amount : 0, entry.getAmount()));
                for (EntityGeneticRegistry.Entry<?, ?> registryEntry : EntityGeneticRegistry.INSTANCE.gatherEntry(e, null)) {
                    counter.compute(registryEntry.getType(), (type, amount) -> (amount == null ? 0 : amount) + entry.getAmount() / 100D);
                }

                if(e == EntityType.TROPICAL_FISH) {
                    DyeColor dyeColor = DyeColor.byName(entry.getVariant(), null);
                    if(dyeColor != null) {
                        tropicalFishColours.put(dyeColor, entry.getAmount());
                    }
                }
            });
        }

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
                tropicalFishColours.values().stream().mapToDouble(Integer::doubleValue).sum() / DyeColor.values().length / 100D,
                Arrays.stream(DyeColor.values())
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

        public TranslationTextComponent getTranslation() {
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
        List<Pair<TranslationTextComponent, Double>> parts;
    }

    public enum DriveType {
        DINOSAUR,
        OTHER
    }

}
