package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.EntityStorageOverrides;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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

        this.addComponent(EntityComponentTypes.METABOLISM)
                .setDistanceSmellFood(30)
                .setDiet(new FeedingDiet()
                        .add(new ItemStack(Items.APPLE))
                )
                .setMaxFood(7500)
                .setMaxWater(6000);

        this.addComponent(ComponentHandler.MULTIPART, EntityStorageOverrides.DINOSAUR_MULTIPART)
            .addCubesForAge(ADULT_AGE,
                "tail4", "tail3", "tail2", "tail1",
                "hips", "chest",
                "neck1", "neck2", "neck3",
                "head", "jawUpper1",
                "legUpperRight", "legMiddleRight", "legLowerRight",
                "legUpperLeft", "legMiddleLeft", "legLowerLeft"
            );

        this.addComponent(EntityComponentTypes.ANIMATION);

        this.addComponent(ComponentHandler.ITEM_DROPS)
            .addFossils("foot", "claw", "leg", "neck", "pelvis", "ribcage", "skull", "tail");

        this.addComponent(EntityComponentTypes.RENDER_ADJUSTMENTS);

        this.addComponent(EntityComponentTypes.GENDER);
        this.addComponent(ComponentHandler.AGE)
            .addStage(new AgeStage(CHILD_AGE, 72000, ADULT_AGE))
            .addStage(new AgeStage(ADULT_AGE, -1, ADULT_AGE).setCanBreed(true))
            .addStage(new AgeStage(SKELETON_AGE, -1, SKELETON_AGE))
            .setDefaultStageName(ADULT_AGE);

        this.addComponent(EntityComponentTypes.MODEL).setShadowSize(1F);
        this.addComponent(EntityComponentTypes.SPEED_TRACKING);
        this.addComponent(EntityComponentTypes.HERD)
                .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));
        this.addComponent(ComponentHandler.WANDER_AI);
        this.addComponent(ComponentHandler.ATTACK_AI);

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
            .addGeneticEntry(GeneticTypes.SPEED_MODIFIER, "movement_speed",0, 0.75F);

        this.addComponent(EntityComponentTypes.GENETIC_LAYER_COLORS)
            .addLayer("base", "base")
            .addLayer("belly", "belly")
            .addLayer("overlays", "frills", "patterns", "outlines", "spots");

        this.addComponent(EntityComponentTypes.FLATTENED_LAYER)
            .staticLayer("claws", 5F)
            .staticLayer("mouth", 5F)
            .staticLayer("nostrils", 5F)
            .staticLayer("teeth", 5F);

        this.addComponent(EntityComponentTypes.BLINKING)
            .setEyesOnTexture("eyes")
            .setEyesOffTexture("eyes_closed")
            .setTickTimeOpen(25)
            .setTickTimeClose(5);

        this.addComponent(EntityComponentTypes.BREEDING);
        this.addComponent(ComponentHandler.DINOSAUR_EGG_LAYING)
            .setEggAmount(7F, 1F)
            .setTicksPregnant(192000, 12000) //8 Minecraft days, give or take half a day
            .setTicksEggHatch(24000, 2000); //1 Minecraft day, give or take 100 seconds
    }
}