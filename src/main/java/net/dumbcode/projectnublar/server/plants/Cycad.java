package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityStorageOverrides;
import net.minecraftforge.common.EnumPlantType;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;

public class Cycad extends Plant {

    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN)
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setRandomizedProperties(Lists.newArrayList("age"))
                .setPlantType(EnumPlantType.Plains)
                .setGroupSpawnSize(5)
                .setChancePerStatePerChunk(0.025F);

        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_PLACEABLE, EntityStorageOverrides.PLANT_PLACEABLE)
                .setPlantType(EnumPlantType.Plains);

        this.attachProperty("age")
                .attachOverride("0", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("1"))
                .attachOverride("1", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("2"))
                .attachOverride("2", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("3"))
                .attachOverride("3", a -> {});
    }
}
