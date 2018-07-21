package net.dumbcode.projectnublar.server.dinosaur;

import lombok.val;
import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;

public class Velociraptor extends Dinosaur {

    public Velociraptor() {
        val map = getModelProperties().getMainModelMap();
        map.put(GrowthStage.ADULT, "velociraptor_adult_idle");

        ItemProperties itemProperties = this.getItemProperties();
        itemProperties.setCookedMeatHealAmount(10);
        itemProperties.setCookedMeatSaturation(1f);
        itemProperties.setRawMeatHealAmount(4);
        itemProperties.setRawMeatSaturation(0.6f);
    }

}
