package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;

public class Cycad extends Plant {

    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN)
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setGroupSpawnSize(5)
                .setChancePerChunk(0.1F);

        this.attachOverride("age_0", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_1"));
        this.attachOverride("age_1", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_2"));
        this.attachOverride("age_2", a -> a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_3"));
        this.attachOverride("age_3", a -> {});
    }
}
