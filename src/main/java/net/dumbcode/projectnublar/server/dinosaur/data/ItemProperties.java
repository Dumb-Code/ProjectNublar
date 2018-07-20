package net.dumbcode.projectnublar.server.dinosaur.data;

import lombok.Data;

@Data
public class ItemProperties {

    private final ItemEggColor maleEggColor = new ItemEggColor();
    private final ItemEggColor femaleEggColor = new ItemEggColor();

    private int cookedMeatHealAmount;
    private int rawMeatHealAmount;
    private float cookedMeatSaturation;
    private float rawMeatSaturation;
}
