package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.item.ItemComponentAccessCreatable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;
import static net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher.create;


public class SerennaVeriformans extends Plant {
    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN)
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setGroupSpawnSize(3)
                .setChancePerChunk(0.05F);


        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_DROPS)
                .setCreatables(Lists.newArrayList(
                        new ItemComponentAccessCreatable()
                        .setAttacher(create(a -> {
                            a.addComponent(EntityComponentTypes.ITEM_RENDER)
                                    .setLocation(new ResourceLocation("apple"));
                            a.addComponent(EntityComponentTypes.ITEM_EATEN)
                                    .setFillAmount(2)
                                    .setDuration(16)
                                    .setSaturation(0.3F)
                                    .setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 200, 0)));
                        }))
                ));


        this.stateOverrides.put("age_0", create(a -> {
            a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("age_1");
            a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 100, 0)));
        }));
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
