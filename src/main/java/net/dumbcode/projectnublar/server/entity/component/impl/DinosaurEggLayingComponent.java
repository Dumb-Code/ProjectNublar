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
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class DinosaurEggLayingComponent extends EntityComponent implements BreedingResultComponent {

    private static final Random RANDOM = new Random();

    private final List<DinosaurEggType> eggTypes = new ArrayList<>();

    private GuassianValue eggAmount = new GuassianValue(1, 0);
    private GuassianValue eggModifier = new GuassianValue(1, 0);
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
                        this.eggModifier.getRandomValue(RANDOM),
                        this.eggTypes.get(RANDOM.nextInt(this.eggTypes.size())),
                        100,
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
        compound.setTag("egg_amount", GuassianValue.writeToNBT(this.eggAmount));
        compound.setTag("egg_modifier", GuassianValue.writeToNBT(this.eggModifier));
        compound.setTag("ticks_pregnancy", GuassianValue.writeToNBT(this.ticksPregnancy));
        compound.setTag("ticks_egg_hatch", GuassianValue.writeToNBT(this.ticksEggHatch));

        compound.setTag("egg_types", this.eggTypes.stream().map(DinosaurEggType::writeToNBT).collect(IOCollectors.toNBTTagList()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound nbt) {
        this.eggAmount = GuassianValue.readFromNBT(nbt.getCompoundTag("egg_amount"));
        this.eggModifier = GuassianValue.readFromNBT(nbt.getCompoundTag("egg_modifier"));
        this.ticksPregnancy = GuassianValue.readFromNBT(nbt.getCompoundTag("ticks_pregnancy"));
        this.ticksEggHatch = GuassianValue.readFromNBT(nbt.getCompoundTag("ticks_egg_hatch"));

        this.eggTypes.clear();
        StreamUtils.stream(nbt.getTagList("egg_types", Constants.NBT.TAG_COMPOUND)).map(b -> DinosaurEggType.readFromNBT((NBTTagCompound) b)).forEach(this.eggTypes::add);

        super.deserialize(nbt);
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
        private GuassianValue eggModifier = new GuassianValue(1, 0);
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
            component.eggModifier = this.eggModifier;
            component.ticksPregnancy = this.ticksPregnancy;
            component.ticksEggHatch = this.ticksEggHatch;
            return component;
        }

        @Override
        public void writeJson(JsonObject json) {
            json.add("egg_amount", GuassianValue.writeToJson(this.eggAmount));
            json.add("egg_modifier", GuassianValue.writeToJson(this.eggModifier));
            json.add("ticks_pregnancy", GuassianValue.writeToJson(this.ticksPregnancy));
            json.add("ticks_egg_hatch", GuassianValue.writeToJson(this.ticksEggHatch));

            json.add("egg_types", this.eggTypes.stream().map(DinosaurEggType::writeToJson).collect(IOCollectors.toJsonArray()));
        }

        @Override
        public void readJson(JsonObject json) {
            this.eggAmount = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "egg_amount"));
            this.eggModifier = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "egg_modifier"));
            this.ticksPregnancy = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "ticks_pregnancy"));
            this.ticksEggHatch = GuassianValue.readFromJson(JsonUtils.getJsonObject(json, "ticks_egg_hatch"));

            this.eggTypes.clear();
            StreamUtils.stream(JsonUtils.getJsonArray(json, "egg_types")).map(JsonElement::getAsJsonObject).map(DinosaurEggType::readFromJson).forEach(this.eggTypes::add);
        }
    }
}
