package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityStorageOverrides;
import net.dumbcode.dumblibrary.server.ecs.item.ItemComponentAccessCreatable;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.EnumPlantType;

import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;
import static net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher.create;


public class SerennaVeriformans extends Plant {
    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN)
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setRandomizedProperties(Lists.newArrayList("age"))
                .setPlantType(EnumPlantType.Plains)
                .setGroupSpawnSize(3)
                .setChancePerStatePerChunk(0.0013F);


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

        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_PLACEABLE, EntityStorageOverrides.PLANT_PLACEABLE)
                .setPlantType(EnumPlantType.Plains);

        this.attachProperty("age")
                .attachOverride("0", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("1");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 100, 0)));
                })
                .attachOverride("1", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("2");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 120, 0)));
                })
                .attachOverride("2", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING).setGrowTo("3");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 160, 1)));
                })
                .attachOverride("3", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT).setPotionEffectList(Lists.newArrayList(new PotionEffect(MobEffects.POISON, 180, 1)));
                });
    }
}
