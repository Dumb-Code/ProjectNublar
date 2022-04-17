package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.dumblibrary.server.utils.GaussianValue;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.dinosaur.eggs.EnumDinosaurEggTypes;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.EntityStorageOverrides;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

public class Dilophosaurus extends Dinosaur {

    public Dilophosaurus() {

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
                .add(500, 20, new ItemStack(Items.APPLE))
                .add(500, 20, Blocks.SPONGE)
                .add(500, 20, EntityType.ARROW)
            )
            .setMaxFood(18000)
            .setMaxWater(18000)

            .setHydrateAmountPerTick(500)

            .setFoodTicks(32)
            .setWaterTicks(18);

        this.addComponentWithOverride(ComponentHandler.MULTIPART.get(), EntityStorageOverrides.DINOSAUR_MULTIPART)
            .addCubesForAge(ADULT_AGE,
                "tail4", "tail3", "tail2", "tail1",
                "hips", "chest",
                "neck1", "neck2", "neck3",
                "head", "jawUpper1",
                "legUpperRight", "legMiddleRight", "legLowerRight",
                "legUpperLeft", "legMiddleLeft", "legLowerLeft"
            );

        this.addEmptyComponent(ComponentHandler.MOOD);
        this.addEmptyComponent(EntityComponentTypes.ANIMATION);

        this.addComponent(ComponentHandler.ITEM_DROPS)
            .addFossils("foot", "claw", "leg", "neck", "pelvis", "ribcage", "skull", "tail");

        this.addComponent(EntityComponentTypes.RENDER_ADJUSTMENTS);

        this.addEmptyComponent(EntityComponentTypes.GENDER);
        this.addEmptyComponent(ComponentHandler.GOAL);
        this.addComponent(ComponentHandler.AGE)
            .addStage(new AgeStage(CHILD_AGE, 72000, ADULT_AGE))
            .addStage(new AgeStage(ADULT_AGE, -1, ADULT_AGE).setCanBreed(true))
            .addStage(new AgeStage(SKELETON_AGE, -1, SKELETON_AGE))
            .setDefaultStageName(ADULT_AGE);

        this.addEmptyComponent(EntityComponentTypes.RENDER_CONTEXT);
        this.addComponent(EntityComponentTypes.MODEL).setShadowSize(1F);
        this.addComponent(EntityComponentTypes.HERD)
            .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));
        this.addEmptyComponent(ComponentHandler.WANDER_AI);
        this.addEmptyComponent(ComponentHandler.ATTACK_AI);
        this.addComponent(ComponentHandler.DEFENSE).setBaseDefense(2D);

        this.addComponent(ComponentHandler.SKELETAL_BUILDER)
            .initializeMap(
                "foot", "legLowerLeft",
                "foot", "legLowerRight",
                "leg", "legUpperLeft",
                "leg", "legUpperRight",
                "ribs", "hips",
                "tail", "tail4",
                "neck", "neck3",
                "skull", "head"
            );

        this.addComponent(EntityComponentTypes.GENETICS)
            .addGeneticEntry(GeneticTypes.SPEED_MODIFIER.get());

        this.addComponent(EntityComponentTypes.GENETIC_LAYER_COLORS)
            .addLayer(GeneticLayerEntry.builder("base", 0F)
                .colorMutateMin(0.85F)
                .layerWeight(0.65F)
            )
            .addLayer(GeneticLayerEntry.builder("belly", 0.1F)
                .colorMutateMin(0.85F)
                .secondaryColours()
            )
            .addLayer(GeneticLayerEntry.builder("frills", 0.2F)
                .colorMutateMin(0.85F)
                .secondaryColours()
                .layerWeight(0.2F)
            )
            .addLayer(GeneticLayerEntry.builder("patterns", 0.3F)
                .colorMutateMin(0.85F)
                .secondaryColours()
                .variesOpacity()
                .layerWeight(0.65F)
            )
            .addLayer(GeneticLayerEntry.builder("outlines", 0.4F)
                .colorMutateMin(0.85F)
                .variesOpacity()
                .layerWeight(0.25F)
            )
            .addLayer(GeneticLayerEntry.builder("spots", 0.5F)
                .colorMutateMin(0.85F)
                .variesOpacity()
                .secondaryColours()
            );

        this.addComponent(EntityComponentTypes.FLATTENED_LAYER)
            .staticLayer("claws", 5F)
            .staticLayer("mouth", 5F)
            .staticLayer("nostrils", 5F)
            .staticLayer("teeth", 5F);

        this.addComponent(EntityComponentTypes.EYES_CLOSED)
            .setIndex(10F)
            .setEyesOpenTexture("eyes")
            .setEyesClosedTexture("eyes_closed");

        this.addComponent(EntityComponentTypes.BLINKING)
            .setTickTimeOpen(25)
            .setTickTimeClose(5);

        this.addComponent(EntityComponentTypes.CULL_SIZE.get())
            .setHeight(2.8F)
            .setWidth(6F);

        this.addComponent(EntityComponentTypes.BREEDING)
            .setMinTicksBetweenBreeding(240000); //10 Minecraft days

        this.addComponent(ComponentHandler.DINOSAUR_EGG_LAYING)
            .addEggType(EnumDinosaurEggTypes.NORMAL.getType())
            .setEggModifier(new GaussianValue(1F, 0.1F))
            .setEggAmount(new GaussianValue(7F, 1F))
            .setTicksPregnancy(new GaussianValue(192000, 12000)) //8 Minecraft days, give or take half a day
            .setTicksEggHatch(new GaussianValue(24000, 2000)); //1 Minecraft day, give or take 100 seconds

        this.addComponent(EntityComponentTypes.SLEEPING)
            .setSleepingAnimation(new ResourceLocation(ProjectNublar.MODID, "resting"))
            .setWakeupTime(1000)
            .setSleepTime(13000);


        this.addEmptyComponent(ComponentHandler.TRACKING_DATA);
        this.addEmptyComponent(ComponentHandler.BASIC_ENTITY_INFORMATION);
    }
}