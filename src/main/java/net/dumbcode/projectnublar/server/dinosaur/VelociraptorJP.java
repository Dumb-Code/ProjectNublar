package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ECSSounds;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.utils.GaussianValue;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.entity.EntityStorageOverrides;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.sounds.SoundHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

public class VelociraptorJP extends Dinosaur {

    public VelociraptorJP() {

        getItemProperties()
            .setCookedMeatHealAmount(10)
            .setCookedMeatSaturation(1f)
            .setRawMeatHealAmount(4)
            .setRawMeatSaturation(0.6f)
            .setCookingExperience(1f);

        DinosaurInformation dinosaurInfomation = this.getDinosaurInfomation();
        dinosaurInfomation.setCanClimb(false);
        dinosaurInfomation.setPeriod(DinosaurPeriod.CRETACEOUS);
        dinosaurInfomation.getBiomeTypes().addAll(Lists.newArrayList(
            BiomeDictionary.Type.CONIFEROUS,
            BiomeDictionary.Type.DRY,
            BiomeDictionary.Type.PLAINS,
            BiomeDictionary.Type.MESA,
            BiomeDictionary.Type.FOREST,
            BiomeDictionary.Type.MOUNTAIN
        ));
    }

    @Override
    public void attachDefaultComponents() {

        this.addComponent(ComponentHandler.METABOLISM)
            .setDistanceSmellFood(30)
            .setDiet(new FeedingDiet()
                .add(250, 30, EntityType.CHICKEN, EntityType.RABBIT)
                .add(1000, 100, EntityType.SHEEP, EntityType.COW, EntityType.PIG)
                .add(5000, 300, EntityType.HORSE)

                .add(250, 20, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.BEEF, Items.COOKED_BEEF, Items.MUTTON, Items.COOKED_MUTTON)
                .add(150, 15, Items.CHICKEN, Items.COOKED_CHICKEN, Items.RABBIT, Items.COOKED_RABBIT)
            )
            .setMaxFood(18000)
            .setMaxWater(10000)

            .setHydrateAmountPerTick(500)

            .setFoodTicks(100)
            .setWaterTicks(190);

        this.addComponentWithOverride(ComponentHandler.MULTIPART.get(), EntityStorageOverrides.DINOSAUR_MULTIPART)
                .addCubesForAge(ADULT_AGE,
                        "tail4", "tail3", "tail2", "tail1",
                        "legUpperRight", "legMiddleRight", "legLowerRight",
                        "legUpperLeft", "legMiddleLeft", "legLowerLeft",
                        "neck2", "hips", "chest", "jawUpper1", "head"
                );

        this.addEmptyComponent(ComponentHandler.MOOD);
        this.addEmptyComponent(EntityComponentTypes.ANIMATION);

        this.addComponent(EntityComponentTypes.RENDER_ADJUSTMENTS)
            .setScaleX(0.75F)
            .setScaleY(0.75F)
            .setScaleZ(0.75F);

        this.addEmptyComponent(EntityComponentTypes.GENDER);
        this.addComponent(ComponentHandler.AGE)
            .addStage(new AgeStage(CHILD_AGE, 72000, ADULT_AGE))
            .addStage(new AgeStage(ADULT_AGE, -1, ADULT_AGE).setCanBreed(true))
            .addStage(new AgeStage(SKELETON_AGE, -1, SKELETON_AGE))
            .setDefaultStageName(ADULT_AGE);

        this.addEmptyComponent(EntityComponentTypes.RENDER_CONTEXT);
        this.addComponent(EntityComponentTypes.MODEL).setShadowSize(2F);
        this.addEmptyComponent(EntityComponentTypes.SPEED_TRACKING);
        this.addComponent(EntityComponentTypes.HERD)
            .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));

        this.addEmptyComponent(ComponentHandler.WANDER_AI);
        this.addEmptyComponent(ComponentHandler.ATTACK_AI);
        this.addComponent(ComponentHandler.DEFENSE).setBaseDefense(2D);

        this.addComponent(EntityComponentTypes.GENETICS)
                .addGeneticEntry(GeneticTypes.SPEED_MODIFIER.get(), "movement_speed", -1, 0.2F);

        this.addComponent(EntityComponentTypes.GENETIC_LAYER_COLORS)
            .addLayer("body", 0F)
            .addLayer("belly", 0.1F)
            .addLayer("stripes", 0.2F);

        this.addComponent(EntityComponentTypes.FLATTENED_LAYER)
            .staticLayer("claws", 5F)
            .staticLayer("base_extra", 5F)
            .staticLayer("mouth", 5F);

        this.addComponent(EntityComponentTypes.EYES_CLOSED)
            .setIndex(10F)
            .setEyesOnTexture("eyes")
            .setEyesOffTexture("eyes_closed");

        this.addComponent(EntityComponentTypes.BLINKING)
            .setTickTimeOpen(100)
            .setTickTimeClose(5);

        this.addComponent(EntityComponentTypes.BREEDING)
            .setMinTicksBetweenBreeding(240000); //10 Minecraft days

        this.addComponent(ComponentHandler.DINOSAUR_EGG_LAYING)
            .addEggType(EnumDinosaurEggTypes.NORMAL.getType())
            .addEggType(EnumDinosaurEggTypes.VELOCIRAPTOR.getType())
            .setEggModifier(new GaussianValue(1F, 0.1F))
            .setEggAmount(new GaussianValue(7F, 1F))
            .setTicksPregnancy(new GaussianValue(192000, 12000)) //8 Minecraft days, give or take half a day
            .setTicksEggHatch(new GaussianValue(24000, 2000)); //1 Minecraft day, give or take 100 seconds

        this.addComponent(EntityComponentTypes.SLEEPING)
            .setSleepingAnimation(new ResourceLocation(ProjectNublar.MODID, "sleeping"))
            .setWakeupTime(0)
            .setSleepTime(13000);


        this.addEmptyComponent(ComponentHandler.TRACKING_DATA);
        this.addComponent(EntityComponentTypes.IDLE_ACTION)
            .setIdleAnimations(Lists.newArrayList(() -> AnimationHandler.LOOK_AROUND, () -> AnimationHandler.SCRATCHING, () -> AnimationHandler.SNIFF_AIR, () -> AnimationHandler.SNIFF_GROUND))
            .setSittingAnimation(() -> AnimationHandler.SITTING)
            .setMovementAnimations(Lists.newArrayList(() -> AnimationHandler.LEFT_CLAW, () -> AnimationHandler.RIGHT_CLAW, () -> AnimationHandler.LOOK_LEFT, () -> AnimationHandler.LOOK_RIGHT));
        this.addEmptyComponent(ComponentHandler.BASIC_ENTITY_INFORMATION);
        this.addComponentWithOverride(EntityComponentTypes.CLOSE_PROXIMITY_ANGRY.get(), EntityStorageOverrides.CLOSE_PROXIMITY_BLACKLIST)
            .setRange(3)
            .add(EntityHandler.DINOSAUR.getId());

        this.addComponent(EntityComponentTypes.SOUND_STORAGE)
            .addSound(ECSSounds.EATING_CRUNCH, SoundHandler.VELOCIRAPTOR_FLESH_CRUNCH)
            .addSound(ECSSounds.EATING_RIP, SoundHandler.VELOCIRAPTOR_FLESH_RIP)
            .addSound(ECSSounds.DRINKING, SoundHandler.VELOCIRAPTOR_DRINK)
            .addSound(ECSSounds.ATTACKING, SoundHandler.VELOCIRAPTOR_BITE, SoundHandler.VELOCIRAPTOR_BITE, SoundHandler.VELOCIRAPTOR_BITE, SoundHandler.VELOCIRAPTOR_SCREECH, SoundHandler.VELOCIRAPTOR_CALL)
            .addSound(ECSSounds.IDLE, SoundHandler.VELOCIRAPTOR_IDLE)
            .addSound(ECSSounds.CALLING, SoundHandler.VELOCIRAPTOR_CALL);
    }
}