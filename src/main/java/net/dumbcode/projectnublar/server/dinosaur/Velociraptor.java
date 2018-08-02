package net.dumbcode.projectnublar.server.dinosaur;

import lombok.val;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.SkeletalInformation;

public class Velociraptor extends Dinosaur {

    public Velociraptor() {
        val map = getModelProperties().getMainModelMap();
        map.put(GrowthStage.ADULT, "velociraptor_adult_idle");

        ItemProperties itemProperties = this.getItemProperties();
        itemProperties.setCookedMeatHealAmount(10);
        itemProperties.setCookedMeatSaturation(1f);
        itemProperties.setRawMeatHealAmount(4);
        itemProperties.setRawMeatSaturation(0.6f);

        SkeletalInformation skeletalInformation = this.getSkeletalInformation();
        skeletalInformation.initilizeMap(
                "foot", "Right upper foot",
                "foot", "Left upper foot",
                "leg", "Right thigh",
                "leg", "Left thigh",
                "chest", "body3",
                "tail", "tail1",
                "shoulders", "body2",
                "arm", "Right arm",
                "arm", "Left arm",
                "hand", "Right hand",
                "hand", "Left hand",
                "neck", "body1",
                "head", "neck5"
        );
    }

}
