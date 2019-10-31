package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import java.util.List;
import java.util.Map;

public class Dilophosaurus extends Dinosaur {

    public Dilophosaurus() {

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
                        .add(new ItemStack(Items.APPLE)))
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


        this.addComponent(EntityComponentTypes.GENDER);
        this.addComponent(ComponentHandler.AGE)
                .addStage(new AgeStage(ADULT_AGE, -1))
                .addStage(new AgeStage(SKELETON_AGE, -1));

        this.addComponent(EntityComponentTypes.MODEL);
        this.addComponent(EntityComponentTypes.SPEED_TRACKING);
        this.addComponent(EntityComponentTypes.HERD)
                .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));
        this.addComponent(ComponentHandler.WANDER_AI);

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
    }
}