package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;

public class SerennaVeriformans extends Plant {
    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN)
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setGroupSpawnSize(3)
                .setChancePerChunk(0.05F);

        this.attachOverride("age_0", a -> {
            a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_1");
            a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 100, 0)));
        });
        this.attachOverride("age_1", a -> {
            a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_2");
            a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 120, 0)));
        });
        this.attachOverride("age_2", a -> {
            a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_3");
            a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 160, 1)));
        });
        this.attachOverride("age_3", a -> {
            a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 180, 1)));
        });
    }
}
