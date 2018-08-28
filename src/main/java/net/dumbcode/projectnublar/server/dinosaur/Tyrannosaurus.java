package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import lombok.val;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInfomation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.dinosaur.data.SkeletalInformation;
import net.minecraftforge.common.BiomeDictionary;

public class Tyrannosaurus extends Dinosaur {

    public Tyrannosaurus() {
        val map = getModelProperties().getMainModelMap();
        map.put(GrowthStage.ADULT, "tyrannosaurus_adult_idle");

        getItemProperties()
                .setCookedMeatHealAmount(10)
                .setCookedMeatSaturation(1f)
                .setRawMeatHealAmount(4)
                .setRawMeatSaturation(0.6f)
                .setCookingExperience(1f);

        SkeletalInformation skeletalInformation = this.getSkeletalInformation();
        skeletalInformation.initializeMap(
                "foot", "legLowerLeft",
                "foot", "legLowerRight",
                "leg", "legUpperLeft",
                "leg", "legUpperRight",
                "body", "hips"
        );

        DinosaurInfomation dinosaurInfomation = this.getDinosaurInfomation();
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

}