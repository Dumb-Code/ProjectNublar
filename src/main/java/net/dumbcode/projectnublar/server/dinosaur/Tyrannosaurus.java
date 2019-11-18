package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityStorageOverrides;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;
import java.util.Map;

public class Tyrannosaurus extends Dinosaur {

    public Tyrannosaurus() {

        getItemProperties()
                .setCookedMeatHealAmount(10)
                .setCookedMeatSaturation(1f)
                .setRawMeatHealAmount(4)
                .setRawMeatSaturation(0.6f)
                .setCookingExperience(1f);

        DinosaurInformation dinosaurInfomation = this.getDinosaurInfomation();
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
                        .add(DinosaurEntity.class)
                )
                .setMaxFood(7500)
                .setMaxWater(6000);

        this.addComponent(ComponentHandler.MULTIPART, EntityStorageOverrides.DINOSAUR_MULTIPART)
            .addCubesForAge(ADULT_AGE,
                "tail4", "Tail3", "tail2", "tail1",
                "legUpperRight", "legMiddleRight", "legLowerRight",
                "legUpperLeft", "legMiddleLeft", "legLowerLeft",
                "neck3", "hips", "chest", "jawUpper1", "head"
            );

        this.addComponent(EntityComponentTypes.ANIMATION);

        this.addComponent(ComponentHandler.ITEM_DROPS)
                .addFossils("foot", "claw", "leg", "neck", "pelvis", "ribcage", "skull", "tail");


        this.addComponent(EntityComponentTypes.RENDER_ADJUSTMENTS)
                .setScaleX(2.5F)
                .setScaleY(2.5F)
                .setScaleZ(2.5F);


        this.addComponent(EntityComponentTypes.GENDER);
        this.addComponent(ComponentHandler.AGE)
                .addStage(new AgeStage(ADULT_AGE, -1))
                .addStage(new AgeStage(SKELETON_AGE, -1));

        this.addComponent(EntityComponentTypes.MODEL);
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
                        "ribs", "ribcage",
                        "tail", "tail4",
                        "neck", "neck3",
                        "skull", "head"
                );

        this.addComponent(EntityComponentTypes.GENETICS)
            .addGeneticEntry(GeneticTypes.SPEED_MODIFIER, 0, 0.75F);

        this.addComponent(EntityComponentTypes.GENETIC_LAYER_COLORS)
            .addLayer("body", "body")
            .addLayer("stripes", "stripes");

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
    }
}