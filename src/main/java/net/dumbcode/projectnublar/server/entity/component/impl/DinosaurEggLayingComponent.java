package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.BreedingResultComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.GaussianValue;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.PregnancyInformation;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DinosaurEggLayingComponent extends EntityComponent implements BreedingResultComponent, TrackingDataComponent {

    private static final Random RANDOM = new Random();

    private final List<DinosaurEggType> eggTypes = new ArrayList<>();

    private GaussianValue eggAmount = new GaussianValue(1, 0);
    private GaussianValue eggModifier = new GaussianValue(1, 0);
    private GaussianValue ticksPregnancy = new GaussianValue(1, 0);
    private GaussianValue ticksEggHatch = new GaussianValue(1, 0);

    @Getter
    private final List<EggEntry> heldEggs = new ArrayList<>();

    @Override
    public void onBreed(ComponentAccess self, ComponentAccess other) {
        if(!self.getOrExcept(EntityComponentTypes.GENDER).male) {
            Optional<GeneticComponent> thisGenetics = self.get(EntityComponentTypes.GENETICS);
            Optional<GeneticComponent> otherGenetics = other.get(EntityComponentTypes.GENETICS);
            if(thisGenetics.isPresent() && otherGenetics.isPresent()) {
                int pregnancyTime = (int) this.ticksPregnancy.getRandomValue(RANDOM);
                int eggs = (int) this.eggAmount.getRandomValue(RANDOM);
                for (int i = 0; i < eggs; i++) {
                    this.heldEggs.add(new EggEntry(
                        this.eggModifier.getRandomValue(RANDOM),
                        this.eggTypes.get(RANDOM.nextInt(this.eggTypes.size())),
                        pregnancyTime, //Set to 100 for debugging
                        (int) this.ticksEggHatch.getRandomValue(RANDOM),
                        this.generateCombinedGenetics(thisGenetics.get(), otherGenetics.get())
                    ));
                }
            }
        }
    }

    //TODO: handle random genetic deviation
    private List<GeneticEntry<?, ?>> generateCombinedGenetics(GeneticComponent component, GeneticComponent otherComponent) {
        List<GeneticEntry<?, ?>> combinedGenetics = Lists.newArrayList();

        Set<GeneticEntry<?, ?>> handledGenetics = Sets.newHashSet();

        for (GeneticEntry<?, ?> genetic : component.getGenetics()) {
            Optional<GeneticEntry<?, ?>> entryOptional = otherComponent.findEntry(genetic);
            if(entryOptional.isPresent()) {
                GeneticEntry<?, ?> entry = entryOptional.get();
                combinedGenetics.add(combine(genetic, entry));
                handledGenetics.add(genetic);
            } else {
                combinedGenetics.add(genetic.copy());
            }
        }

        for (GeneticEntry<?, ?> genetic : otherComponent.getGenetics()) {
            if (!handledGenetics.contains(genetic)) {
                combinedGenetics.add(genetic.copy());
            }
        }

        return combinedGenetics;
    }

    private static <T extends GeneticFactoryStorage<O>, O> GeneticEntry<?, ?> combine(GeneticEntry<?, ?> aRaw, GeneticEntry<?, ?> bRaw) {
        GeneticEntry<T, O> a = (GeneticEntry<T, O>) aRaw;
        GeneticEntry<T, O> b = (GeneticEntry<T, O>) bRaw;
        O o = a.getType().getDataHandler().combineChild(a.getModifier(), b.getModifier());
        return a.copy().setModifier(o);
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.put("egg_amount", GaussianValue.writeToNBT(this.eggAmount));
        compound.put("egg_modifier", GaussianValue.writeToNBT(this.eggModifier));
        compound.put("ticks_pregnancy", GaussianValue.writeToNBT(this.ticksPregnancy));
        compound.put("ticks_egg_hatch", GaussianValue.writeToNBT(this.ticksEggHatch));

        compound.put("egg_types", this.eggTypes.stream().map(DinosaurEggType::writeToNBT).collect(CollectorUtils.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        this.eggAmount = GaussianValue.readFromNBT(nbt.getCompound("egg_amount"));
        this.eggModifier = GaussianValue.readFromNBT(nbt.getCompound("egg_modifier"));
        this.ticksPregnancy = GaussianValue.readFromNBT(nbt.getCompound("ticks_pregnancy"));
        this.ticksEggHatch = GaussianValue.readFromNBT(nbt.getCompound("ticks_egg_hatch"));

        this.eggTypes.clear();
        StreamUtils.stream(nbt.getList("egg_types", Constants.NBT.TAG_COMPOUND)).map(b -> DinosaurEggType.readFromNBT((CompoundNBT) b)).forEach(this.eggTypes::add);

        super.deserialize(nbt);
    }

    @Override
    public void addTrackingData(ComponentAccess access, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> new PregnancyInformation(this.heldEggs.stream().mapToInt(EggEntry::getTicksLeft).toArray()));
    }

    @Data
    @AllArgsConstructor
    public static class EggEntry {
        private final float randomScaleAdjustment;
        private final DinosaurEggType type;
        private int ticksLeft;
        private final int eggTicks;
        private final List<GeneticEntry<?, ?>> combinedGenetics;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<DinosaurEggLayingComponent> {

        private final List<DinosaurEggType> eggTypes = new ArrayList<>();

        private GaussianValue eggAmount = new GaussianValue(1, 0);
        private GaussianValue eggModifier = new GaussianValue(1, 0);
        private GaussianValue ticksPregnancy = new GaussianValue(1, 0);
        private GaussianValue ticksEggHatch = new GaussianValue(1, 0);

        public Storage addEggType(DinosaurEggType... type) {
            Collections.addAll(this.eggTypes, type);
            return this;
        }


        @Override
        public void constructTo(DinosaurEggLayingComponent component) {
            component.eggTypes.addAll(this.eggTypes);
            component.eggAmount = this.eggAmount;
            component.eggModifier = this.eggModifier;
            component.ticksPregnancy = this.ticksPregnancy;
            component.ticksEggHatch = this.ticksEggHatch;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("egg_amount", GaussianValue.writeToJson(this.eggAmount));
            json.add("egg_modifier", GaussianValue.writeToJson(this.eggModifier));
            json.add("ticks_pregnancy", GaussianValue.writeToJson(this.ticksPregnancy));
            json.add("ticks_egg_hatch", GaussianValue.writeToJson(this.ticksEggHatch));

            json.add("egg_types", this.eggTypes.stream().map(DinosaurEggType::writeToJson).collect(CollectorUtils.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.eggAmount = GaussianValue.readFromJson(JSONUtils.getAsJsonObject(json, "egg_amount"));
            this.eggModifier = GaussianValue.readFromJson(JSONUtils.getAsJsonObject(json, "egg_modifier"));
            this.ticksPregnancy = GaussianValue.readFromJson(JSONUtils.getAsJsonObject(json, "ticks_pregnancy"));
            this.ticksEggHatch = GaussianValue.readFromJson(JSONUtils.getAsJsonObject(json, "ticks_egg_hatch"));

            this.eggTypes.clear();
            StreamUtils.stream(JSONUtils.getAsJsonArray(json, "egg_types")).map(JsonElement::getAsJsonObject).map(DinosaurEggType::readFromJson).forEach(this.eggTypes::add);
        }
    }
}
