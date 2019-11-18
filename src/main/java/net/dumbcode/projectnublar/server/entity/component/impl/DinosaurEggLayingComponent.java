package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.BreedingResultComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.GeneticComponent;

import java.util.*;

public class DinosaurEggLayingComponent extends EntityComponent implements BreedingResultComponent {

    @Getter
    private final List<EggEntry> heldEggs = new ArrayList<>();

    @Override
    public void onBreed(ComponentAccess other) {
        if(!this.access.getOrExcept(EntityComponentTypes.GENDER).male) {
            Optional<GeneticComponent> thisGenetics = this.access.get(EntityComponentTypes.GENETICS);
            Optional<GeneticComponent> otherGenetics = other.get(EntityComponentTypes.GENETICS);
            if(thisGenetics.isPresent() && otherGenetics.isPresent()) {
                this.heldEggs.add(new EggEntry(50, this.generateCombinedGenetics(thisGenetics.get(), otherGenetics.get())));//todo: variable ticks taken
            }
        }
    }

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
        private final List<GeneticEntry<?>> combinedGenetics;
    }

    public static class Storage implements EntityComponentStorage<DinosaurEggLayingComponent> {

        private int ticksEggHatch;

        @Override
        public DinosaurEggLayingComponent construct() {
            return new DinosaurEggLayingComponent();
        }

        @Override
        public void writeJson(JsonObject json) {

        }

        @Override
        public void readJson(JsonObject json) {

        }
    }
}
