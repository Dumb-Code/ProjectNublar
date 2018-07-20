package net.dumbcode.projectnublar.server.dinosaur;

import lombok.val;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;

public class Velociraptor extends Dinosaur {

    public Velociraptor() {
        val map = getModelProperties().getMainModelMap();
        map.put(GrowthStage.ADULT, "velociraptor_adult_idle");
    }

}
