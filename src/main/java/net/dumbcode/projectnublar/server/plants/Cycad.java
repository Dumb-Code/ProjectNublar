package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityStorageOverrides;
import net.minecraftforge.common.PlantType;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;

public class Cycad extends Plant {

    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN.get())
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setRandomizedProperties(Lists.newArrayList("age"))
                .setPlantType(PlantType.PLAINS)
                .setGroupSpawnSize(5)
                .setChancePerStatePerChunk(0.025F);

        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_PLACEABLE.get(), EntityStorageOverrides.PLANT_PLACEABLE)
                .setPlantType(PlantType.PLAINS.PLAINS);

        this.attachProperty("age")
                .attachOverride("0", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("1"))
                .attachOverride("1", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("2"))
                .attachOverride("2", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("3"))
                .attachOverride("3", a -> {});
    }
}
