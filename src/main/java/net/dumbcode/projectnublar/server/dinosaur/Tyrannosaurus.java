package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.ecs.component.impl.data.GeneticLayerEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.EntityStorageOverrides;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

public class Tyrannosaurus extends Dinosaur {

    public Tyrannosaurus() {

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
            )
            .setMaxFood(7500)
            .setMaxWater(6000);

        this.addEmptyComponent(ComponentHandler.MOOD);
        this.addEmptyComponent(ComponentHandler.ATTACK_FENCE_AI);

        this.addComponentWithOverride(ComponentHandler.MULTIPART.get(), EntityStorageOverrides.DINOSAUR_MULTIPART)
            .addCubesForAge(ADULT_AGE,
                "tail4", "Tail3", "tail2", "tail1",
                "legUpperRight", "legMiddleRight", "legLowerRight",
                "legUpperLeft", "legMiddleLeft", "legLowerLeft",
                "neck3", "hips", "chest", "jawUpper1", "head"
            );

        this.addEmptyComponent(EntityComponentTypes.ANIMATION);

        this.addComponent(ComponentHandler.ITEM_DROPS)
            .addFossils("foot", "claw", "leg", "neck", "pelvis", "ribcage", "skull", "tail");


        this.addComponent(EntityComponentTypes.RENDER_ADJUSTMENTS)
            .setScaleX(2.5F)
            .setScaleY(2.5F)
            .setScaleZ(2.5F);


        this.addEmptyComponent(EntityComponentTypes.GENDER);
        this.addComponent(ComponentHandler.AGE)
            .addStage(new AgeStage(CHILD_AGE, 72000, ADULT_AGE))
            .addStage(new AgeStage(ADULT_AGE, -1, ADULT_AGE).setCanBreed(true))
            .addStage(new AgeStage(SKELETON_AGE, -1, SKELETON_AGE))
            .setDefaultStageName(ADULT_AGE);

        this.addEmptyComponent(EntityComponentTypes.RENDER_CONTEXT);
        this.addComponent(EntityComponentTypes.MODEL).setShadowSize(3F);
        this.addEmptyComponent(EntityComponentTypes.SPEED_TRACKING);
        this.addComponent(EntityComponentTypes.HERD)
            .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));

        this.addEmptyComponent(ComponentHandler.WANDER_AI);
        this.addEmptyComponent(ComponentHandler.ATTACK_AI);
        this.addComponent(ComponentHandler.DEFENSE).setBaseDefense(3D);

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
            .addGeneticEntry(GeneticTypes.SPEED_MODIFIER.get(), "movement_speed", 0, 0.75F);

        this.addComponent(EntityComponentTypes.GENETIC_LAYER_COLORS)
            .addLayer(GeneticLayerEntry.builder("base", 0F)
                .defaultColorMin(0.85F)
            )
            .addLayer(GeneticLayerEntry.builder("undercolor", 0.1F)
                .checkIfExists()
                .variesOpacity()
                .defaultColorMin(0.85F)
            )
            .addLayer(GeneticLayerEntry.builder("back", 0.2F)
                .checkIfExists()
                .variesOpacity()
            )
            .addLayer(GeneticLayerEntry.builder("belly", 0.3F)
                .defaultColorMin(0.85F)
            )
            .addLayer(GeneticLayerEntry.builder("stripes", 0.4F)
                .variesOpacity()
                .defaultColorMin(0.3F)
            )
            .addLayer(GeneticLayerEntry.builder("stripes_overlay", 0.5F)
                .checkIfExists()
                .variesOpacity()
                .defaultColorMin(0.3F)
            );

        this.addComponent(EntityComponentTypes.FLATTENED_LAYER)
            .staticLayer("small", 5F);

        this.addComponent(EntityComponentTypes.EYES_CLOSED)
            .setIndex(10F)
            .setEyesOnTexture("eyelid");

        this.addComponent(EntityComponentTypes.BLINKING)
            .setTickTimeOpen(25)
            .setTickTimeClose(5);

        this.addComponent(EntityComponentTypes.CULL_SIZE.get())
            .setHeight(2.5F)
            .setWidth(7.5F);
    }
}