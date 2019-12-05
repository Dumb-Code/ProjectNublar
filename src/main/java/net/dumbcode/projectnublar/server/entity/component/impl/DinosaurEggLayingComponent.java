package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import net.dumbcode.dumblibrary.server.json.JsonUtil;
import net.minecraft.util.JsonUtils;

import java.util.*;

public class DinosaurEggLayingComponent extends EntityComponent implements BreedingResultComponent {

    private static final Random RANDOM = new Random();

    private float eggAmountMean;
    private float eggAmountDeviation;

    private float ticksPregnancyMean;
    private float ticksPregnancyDeviation;

    private float ticksEggHatchMean;
    private float ticksEggHatchDeviation;

    @Getter
    private final List<EggEntry> heldEggs = new ArrayList<>();

    @Override
    public void onBreed(ComponentAccess other) {
        if(!this.access.getOrExcept(EntityComponentTypes.GENDER).male) {
            Optional<GeneticComponent> thisGenetics = this.access.get(EntityComponentTypes.GENETICS);
            Optional<GeneticComponent> otherGenetics = other.get(EntityComponentTypes.GENETICS);
            if(thisGenetics.isPresent() && otherGenetics.isPresent()) {
                int pregnancyTime = (int) (this.ticksPregnancyMean + RANDOM.nextGaussian() * this.ticksPregnancyDeviation);
                int eggs = (int) (this.eggAmountMean + RANDOM.nextGaussian() * this.eggAmountDeviation);
                for (int i = 0; i < eggs; i++) {
                    this.heldEggs.add(new EggEntry(
                        pregnancyTime,
                        (int) (this.ticksEggHatchMean+RANDOM.nextGaussian()*this.ticksEggHatchDeviation),
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


    @Data
    @AllArgsConstructor
    public class EggEntry {
        private int ticksLeft;
        private int eggTicks;
        private final List<GeneticEntry<?>> combinedGenetics;
    }

    @Getter
    public static class Storage implements EntityComponentStorage<DinosaurEggLayingComponent> {

        private float eggAmountMean;
        private float eggAmountDeviation;

        private float ticksPregnancyMean = 1;
        private float ticksPregnancyDeviation = 1;

        private float ticksEggHatchMean = 1;
        private float ticksEggHatchDeviation = 1;

        public Storage setEggAmount(float mean, float deviation) {
            this.eggAmountMean = mean;
            this.eggAmountDeviation = deviation;
            return this;
        }

        public Storage setTicksPregnant(float mean, float deviation) {
            this.ticksPregnancyMean = mean;
            this.ticksPregnancyDeviation = deviation;
            return this;
        }

        public Storage setTicksEggHatch(float mean, float deviation) {
            this.ticksEggHatchMean = mean;
            this.ticksEggHatchDeviation = deviation;
            return this;
        }


        @Override
        public DinosaurEggLayingComponent construct() {
            DinosaurEggLayingComponent component = new DinosaurEggLayingComponent();
            component.eggAmountMean = this.eggAmountMean;
            component.eggAmountDeviation = this.eggAmountDeviation;
            component.ticksPregnancyMean = this.ticksPregnancyMean;
            component.ticksPregnancyDeviation = this.ticksPregnancyDeviation;
            component.ticksEggHatchMean = this.ticksEggHatchMean;
            component.ticksEggHatchDeviation = this.ticksEggHatchDeviation;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("egg_amount_mean", this.eggAmountMean);
            json.addProperty("egg_amount_deviation", this.eggAmountDeviation);
            json.addProperty("ticks_pregnancy_mean", this.ticksPregnancyMean);
            json.addProperty("ticks_pregnancy_deviation", this.ticksPregnancyDeviation);
            json.addProperty("ticks_egg_hatch_mean", this.ticksEggHatchMean);
            json.addProperty("ticks_egg_hatch_deviation", this.ticksEggHatchDeviation);
        }

        @Override
        public void readJson(JsonObject json) {
            this.eggAmountMean = JsonUtils.getFloat(json, "egg_amount_mean");
            this.eggAmountDeviation = JsonUtils.getFloat(json, "egg_amount_deviation");
            this.ticksPregnancyMean = JsonUtils.getFloat(json, "ticks_pregnancy_mean");
            this.ticksPregnancyDeviation = JsonUtils.getFloat(json, "ticks_pregnancy_deviation");
            this.ticksEggHatchMean = JsonUtils.getFloat(json, "ticks_egg_hatch_mean");
            this.ticksEggHatchDeviation = JsonUtils.getFloat(json, "ticks_egg_hatch_deviation");
        }
    }
}
