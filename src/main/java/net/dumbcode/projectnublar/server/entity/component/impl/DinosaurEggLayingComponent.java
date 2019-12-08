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
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.BreedingResultComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.dinosaur.eggs.DinosaurEggType;
import net.dumbcode.projectnublar.server.utils.GuassianValue;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.*;

public class DinosaurEggLayingComponent extends EntityComponent implements BreedingResultComponent {

    private static final Random RANDOM = new Random();

    private final List<DinosaurEggType> eggTypes = new ArrayList<>();

    private GuassianValue eggAmount = new GuassianValue(1, 0);
    private GuassianValue eggSize = new GuassianValue(1, 0);
    private GuassianValue ticksPregnancy = new GuassianValue(1, 0);
    private GuassianValue ticksEggHatch = new GuassianValue(1, 0);

    @Getter
    private final List<EggEntry> heldEggs = new ArrayList<>();

    @Override
    public void onBreed(ComponentAccess other) {
        if(!this.access.getOrExcept(EntityComponentTypes.GENDER).male) {
            Optional<GeneticComponent> thisGenetics = this.access.get(EntityComponentTypes.GENETICS);
            Optional<GeneticComponent> otherGenetics = other.get(EntityComponentTypes.GENETICS);
            if(thisGenetics.isPresent() && otherGenetics.isPresent()) {
                int pregnancyTime = (int) this.ticksPregnancy.getRandomValue(RANDOM);
                int eggs = (int) this.eggAmount.getRandomValue(RANDOM);
                for (int i = 0; i < eggs; i++) {
                    this.heldEggs.add(new EggEntry(
                        this.eggSize.getRandomValue(RANDOM),
                        this.eggTypes.get(RANDOM.nextInt(this.eggTypes.size())),
                        pregnancyTime,
                        (int) this.ticksEggHatch.getRandomValue(RANDOM),
                        this.generateCombinedGenetics(thisGenetics.get(), otherGenetics.get())
                    ));
                }
            }
        }
    }

    //TODO: handle random genetic deviation
    private List<GeneticEntry<?>> generateCombinedGenetics(GeneticComponent component, GeneticComponent otherComponent) {
        List<GeneticEntry<?>> combinedGenetics = Lists.newArrayList();

        Set<String> handledGenetics = Sets.newHashSet();

        for (GeneticEntry<?> genetic : component.getGenetics()) {
            Optional<GeneticEntry<?>> entryOptional = otherComponent.findEntry(genetic.getIdentifier());
            if(entryOptional.isPresent()) {
                GeneticEntry<?> entry = entryOptional.get();
                float newValue = genetic.getType().getCombiner().apply(entry.getModifier(), genetic.getModifier());
                combinedGenetics.add(entry.copy().setModifier(newValue));

                handledGenetics.add(genetic.getIdentifier());
            } else {
                combinedGenetics.add(genetic.copy());
            }
        }

        for (GeneticEntry<?> genetic : otherComponent.getGenetics()) {
            if (!handledGenetics.contains(genetic.getIdentifier())) {
                combinedGenetics.add(genetic.copy());
            }
        }

        return combinedGenetics;
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
    }

    @Data
    @AllArgsConstructor
    public static class EggEntry {
        private final float randomScaleAdjustment;
        private final DinosaurEggType type;
        private int ticksLeft;
        private final int eggTicks;
        private final List<GeneticEntry<?>> combinedGenetics;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<DinosaurEggLayingComponent> {

        private final List<DinosaurEggType> eggTypes = new ArrayList<>();

        private GuassianValue eggAmount = new GuassianValue(1, 0);
        private GuassianValue eggSize = new GuassianValue(1, 0);
        private GuassianValue ticksPregnancy = new GuassianValue(1, 0);
        private GuassianValue ticksEggHatch = new GuassianValue(1, 0);

        public Storage addEggType(DinosaurEggType... type) {
            Collections.addAll(this.eggTypes, type);
            return this;
        }


        @Override
        public DinosaurEggLayingComponent constructTo(DinosaurEggLayingComponent component) {
            component.eggTypes.addAll(this.eggTypes);
            component.eggAmount = this.eggAmount;
            component.eggSize = this.eggSize;
            component.ticksPregnancy = this.ticksPregnancy;
            component.ticksEggHatch = this.ticksEggHatch;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("egg_amount", GuassianValue.writeToJson(this.eggAmount));
            json.add("egg_size", GuassianValue.writeToJson(this.eggSize));
            json.add("ticks_pregnancy", GuassianValue.writeToJson(this.ticksPregnancy));
            json.add("ticks_egg_hatch", GuassianValue.writeToJson(this.ticksEggHatch));

            json.add("egg_types", this.eggTypes.stream().map(DinosaurEggType::writeToJson).collect(IOCollectors.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.eggAmount = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "egg_amount"));
            this.eggSize = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "egg_size"));
            this.ticksPregnancy = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "ticks_pregnancy"));
            this.ticksEggHatch = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "ticks_egg_hatch"));

            this.eggTypes.clear();
            StreamUtils.stream(JsonUtils.getJsonArray(json, "egg_types")).map(JsonElement::getAsJsonObject).map(DinosaurEggType::readFromJson).forEach(this.eggTypes::add);
        }
    }
}
