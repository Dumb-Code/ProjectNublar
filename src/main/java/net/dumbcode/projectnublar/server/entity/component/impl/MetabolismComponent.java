package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.ai.DrinkingAI;
import net.dumbcode.projectnublar.server.entity.ai.FeedingAI;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.MoodChangingComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.mood.MoodReason;
import net.dumbcode.projectnublar.server.entity.mood.MoodReasons;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.MetabolismInformation;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class MetabolismComponent extends EntityComponent implements FinalizableComponent, CanBreedComponent, MoodChangingComponent, TrackingDataComponent {

    public static final int METABOLISM_CHANNEL = 61;

    private int food;
    private int water;

    private int maxFood;
    private int maxWater;

    private int foodRate;
    private int waterRate;

    private int foodTicks;
    private int waterTicks;

    private FeedingDiet diet = new FeedingDiet();
    private int foodSmellDistance;
    private int hydrateAmountPerTick;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("food", this.food);
        compound.setInteger("water", this.water);

        compound.setInteger("max_food", this.maxFood);
        compound.setInteger("max_water", this.maxWater);


        compound.setInteger("food_rate", this.foodRate);
        compound.setInteger("water_rate", this.waterRate);

        compound.setInteger("food_ticks", this.foodTicks);
        compound.setInteger("water_ticks", this.waterTicks);

        compound.setInteger("food_smell_distance", this.foodSmellDistance);

        compound.setTag("diet", this.diet.writeToNBT(new NBTTagCompound()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.food = compound.getInteger("food");
        this.water = compound.getInteger("water");

        this.maxFood = compound.getInteger("max_food");
        this.maxWater = compound.getInteger("max_water");

        this.foodRate = compound.getInteger("food_rate");
        this.waterRate = compound.getInteger("water_rate");

        this.foodTicks = compound.getInteger("food_ticks");
        this.waterTicks = compound.getInteger("water_ticks");

        this.foodSmellDistance = compound.getInteger("food_smell_distance");

        this.diet.fromNBT(compound.getCompoundTag("diet"));
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        if(entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.tasks.addTask(2, new FeedingAI(entity, (EntityLiving) entity, this));
            living.tasks.addTask(2, new DrinkingAI(entity, (EntityLiving) entity, this));
        }
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.food > 1000 && this.water > 1000 && otherEntity.get(ComponentHandler.METABOLISM).map(m -> m.food > 1000 && m.water > 1000).orElse(true);
    }

    @Override
    public void applyMoods(BiConsumer<MoodReason, Number> acceptor) {
        float foodDiff = (this.food / (float) this.maxFood) * 4 - 2;
        if(foodDiff != 0) {
            acceptor.accept(foodDiff < 0 ? MoodReasons.STARVED : MoodReasons.FULL, Math.abs(foodDiff));
        }

        float waterDiff = (this.water / (float) this.maxWater) * 4 - 2;
        if(waterDiff  != 0) {
            acceptor.accept(waterDiff < 0 ? MoodReasons.DEHYDRATED : MoodReasons.HYDRATED, Math.abs(waterDiff));
        }
    }

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> new MetabolismInformation(this.food, this.maxFood, this.water, this.maxWater));
    }

    @Accessors(chain = true)
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
        public MetabolismComponent constructTo(MetabolismComponent component) {
            component.food = component.maxFood = this.maxFood;
            component.water = component.maxWater = this.maxWater;
            component.waterRate = this.waterRate;
            component.foodRate = this.foodRate;
            component.foodTicks = this.foodTicks;
            component.waterTicks = this.waterTicks;

            component.foodSmellDistance = this.distanceSmellFood;
            component.hydrateAmountPerTick = this.hydrateAmountPerTick;

            component.diet = this.diet;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            this.maxFood = JsonUtils.getInt(json, "max_food");
            this.maxWater = JsonUtils.getInt(json, "max_water");

            this.waterRate = JsonUtils.getInt(json, "water_rate");
            this.foodRate = JsonUtils.getInt(json, "food_rate");

            this.foodTicks = JsonUtils.getInt(json, "food_ticks");
            this.waterTicks = JsonUtils.getInt(json, "water_ticks");

            this.hydrateAmountPerTick = JsonUtils.getInt(json, "hydrate_amount_per_tick");

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
