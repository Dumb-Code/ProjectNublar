package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.attributes.ModifiableField;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class MetabolismComponent extends EntityComponent implements FinalizableComponent, CanBreedComponent, MoodChangingComponent, TrackingDataComponent {

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
    private int foodSmellDistance;
    private int hydrateAmountPerTick;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setFloat("food", this.food);
        compound.setFloat("water", this.water);

        compound.setTag("max_food", this.maxFood.writeToNBT());
        compound.setTag("max_water", this.maxWater.writeToNBT());

        compound.setTag("food_rate", this.foodRate.writeToNBT());
        compound.setTag("water_rate", this.waterRate.writeToNBT());

        compound.setInteger("food_ticks", this.foodTicks);
        compound.setInteger("water_ticks", this.waterTicks);

        compound.setInteger("food_smell_distance", this.foodSmellDistance);
        compound.setInteger("hydrate_amount_per_tick", this.hydrateAmountPerTick);

        compound.setTag("diet", this.diet.writeToNBT(new NBTTagCompound()));
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        this.food = compound.getFloat("food");
        this.water = compound.getFloat("water");

        this.maxFood.readFromNBT(compound.getCompoundTag("max_food"));
        this.maxWater.readFromNBT(compound.getCompoundTag("max_water"));

        this.foodRate.readFromNBT(compound.getCompoundTag("food_rate"));
        this.waterRate.readFromNBT(compound.getCompoundTag("water_rate"));

        this.foodTicks = compound.getInteger("food_ticks");
        this.waterTicks = compound.getInteger("water_ticks");

        this.foodSmellDistance = compound.getInteger("food_smell_distance");
        this.hydrateAmountPerTick = compound.getInteger("hydrate_amount_per_tick");

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
