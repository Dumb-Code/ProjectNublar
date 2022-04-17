package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.EntityGoalSupplier;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.ai.DrinkingGoal;
import net.dumbcode.projectnublar.server.entity.ai.FeedingGoal;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.MoodChangingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.mood.MoodReason;
import net.dumbcode.projectnublar.server.entity.mood.MoodReasons;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.MetabolismInformation;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class MetabolismComponent extends EntityComponent implements CanBreedComponent, MoodChangingComponent, TrackingDataComponent, EntityGoalSupplier {

    public static final int METABOLISM_CHANNEL = 61;

    private float food;
    private float water;

    private final ModifiableField maxFood = new ModifiableField();
    private final ModifiableField maxWater = new ModifiableField();

    private final ModifiableField foodRate = new ModifiableField();
    private final ModifiableField waterRate = new ModifiableField();

    private int foodTicks;
    private int waterTicks;

    private FeedingDiet diet = new FeedingDiet();
    @Getter(AccessLevel.NONE)
    private Map<UUID, FeedingDiet> geneticDiets = new HashMap<>(); //We don't need to serialize.
    private FeedingDiet cached;

    private int foodSmellDistance;
    private int hydrateAmountPerTick;

    public FeedingDiet getDiet() {
        if(this.cached != null) {
            return this.cached;
        }
        return FeedingDiet.combine(this.diet, this.geneticDiets.values());
    }

    public void addGeneticDiet(UUID uuid, FeedingDiet diet) {
        this.geneticDiets.put(uuid, diet);
        this.cached = null;
    }

    public void removeGeneticDiet(UUID uuid) {
        this.geneticDiets.remove(uuid);
        this.cached = null;
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putFloat("food", this.food);
        compound.putFloat("water", this.water);

        compound.put("max_food", this.maxFood.writeToNBT());
        compound.put("max_water", this.maxWater.writeToNBT());

        compound.put("food_rate", this.foodRate.writeToNBT());
        compound.put("water_rate", this.waterRate.writeToNBT());

        compound.putInt("food_ticks", this.foodTicks);
        compound.putInt("water_ticks", this.waterTicks);

        compound.putInt("food_smell_distance", this.foodSmellDistance);
        compound.putInt("hydrate_amount_per_tick", this.hydrateAmountPerTick);

        compound.put("diet", this.diet.writeToNBT(new CompoundNBT()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.food = compound.getFloat("food");
        this.water = compound.getFloat("water");

        this.maxFood.readFromNBT(compound.getCompound("max_food"));
        this.maxWater.readFromNBT(compound.getCompound("max_water"));

        this.foodRate.readFromNBT(compound.getCompound("food_rate"));
        this.waterRate.readFromNBT(compound.getCompound("water_rate"));

        this.foodTicks = compound.getInt("food_ticks");
        this.waterTicks = compound.getInt("water_ticks");

        this.foodSmellDistance = compound.getInt("food_smell_distance");
        this.hydrateAmountPerTick = compound.getInt("hydrate_amount_per_tick");

        this.diet.fromNBT(compound.getCompound("diet"));
    }

    @Override
    public void addGoals(GoalManager manager, Consumer<EntityGoal> consumer, ComponentAccess access) {
        if(access instanceof MobEntity) {
            MobEntity living = (MobEntity) access;
            consumer.accept(new FeedingGoal(manager, access, living, this));
            consumer.accept(new DrinkingGoal(manager, access, living, this));
        }
    }


    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.food > 1000 && this.water > 1000 && otherEntity.get(ComponentHandler.METABOLISM).map(m -> m.food > 1000 && m.water > 1000).orElse(true);
    }

    @Override
    public void applyMoods(BiConsumer<MoodReason, Supplier<Float>> acceptor) {
        Supplier<Float> foodDiff = () -> (this.food / (float) this.maxFood.getValue()) * 4 - 2;
        Supplier<Float> waterDiff = () -> (this.water / (float) this.maxWater.getValue()) * 4 - 2;

        acceptor.accept(MoodReasons.STARVED, () -> -MathHelper.clamp(foodDiff.get(), -2, 0));
        acceptor.accept(MoodReasons.FULL, () -> MathHelper.clamp(foodDiff.get(), 0, 2));

        acceptor.accept(MoodReasons.DEHYDRATED, () -> -MathHelper.clamp(waterDiff.get(), -2, 0));
        acceptor.accept(MoodReasons.HYDRATED, () -> MathHelper.clamp(waterDiff.get(), 0, 2));

    }

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> new MetabolismInformation(this.food, (float) this.maxFood.getValue(), this.water, (float) this.maxWater.getValue()));
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Storage implements EntityComponentStorage<MetabolismComponent> {

        // Max Food and water that the ecs can have.
        private int maxFood;
        private int maxWater;

        // Rate that the food and water decrease every second
        private int waterRate = 1;
        private int foodRate = 1;

        //Ticks to eat and drnk
        private int foodTicks;
        private int waterTicks;

        private int distanceSmellFood;
        public int hydrateAmountPerTick;

        private FeedingDiet diet = new FeedingDiet(); //todo serialize

        @Override
        public void constructTo(MetabolismComponent component) {
            component.maxFood.setBaseValue(component.food = this.maxFood);
            component.maxWater.setBaseValue(component.water = this.maxWater);
            component.waterRate.setBaseValue(this.waterRate);
            component.foodRate.setBaseValue(this.foodRate);
            component.foodTicks = this.foodTicks;
            component.waterTicks = this.waterTicks;

            component.foodSmellDistance = this.distanceSmellFood;
            component.hydrateAmountPerTick = this.hydrateAmountPerTick;

            this.diet.copyInto(component.diet);
        }

        @Override
        public void readJson(JsonObject json) {
            this.maxFood = JSONUtils.getAsInt(json, "max_food");
            this.maxWater = JSONUtils.getAsInt(json, "max_water");

            this.waterRate = JSONUtils.getAsInt(json, "water_rate");
            this.foodRate = JSONUtils.getAsInt(json, "food_rate");

            this.foodTicks = JSONUtils.getAsInt(json, "food_ticks");
            this.waterTicks = JSONUtils.getAsInt(json, "water_ticks");

            this.hydrateAmountPerTick = JSONUtils.getAsInt(json, "hydrate_amount_per_tick");

            this.distanceSmellFood = json.get("food_smell_distance").getAsInt();
        }

        @Override
        public void writeJson(JsonObject json) {
            json.addProperty("max_food", this.maxFood);
            json.addProperty("max_water", this.maxWater);

            json.addProperty("water_rate", this.waterRate);
            json.addProperty("food_rate", this.foodRate);

            json.addProperty("food_ticks", this.foodTicks);
            json.addProperty("water_ticks", this.waterTicks);

            json.addProperty("hydrate_amount_per_tick", this.hydrateAmountPerTick);

            json.addProperty("food_smell_distance", this.distanceSmellFood);
        }
    }

}
