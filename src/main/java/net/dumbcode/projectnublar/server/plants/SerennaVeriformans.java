package net.dumbcode.projectnublar.server.plants;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.EntityStorageOverrides;
import net.dumbcode.dumblibrary.server.ecs.item.ItemComponentAccessCreatable;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.PlantType;

import static net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher.create;
import static net.minecraftforge.common.BiomeDictionary.Type.JUNGLE;


public class SerennaVeriformans extends Plant {
    @Override
    public void attachComponents() {
        this.baseAttacher.addComponent(EntityComponentTypes.FLOWER_WORLDGEN.get())
                .setBiomeTypes(Lists.newArrayList(JUNGLE.getName()))
                .setRandomizedProperties(Lists.newArrayList("age"))
                .setPlantType(PlantType.PLAINS)
                .setGroupSpawnSize(3)
                .setChancePerStatePerChunk(0.0013F);


        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_DROPS.get())
                .setCreatables(Lists.newArrayList(
                        new ItemComponentAccessCreatable()
                                .setAttacher(create(a -> {
                                    a.addComponent(EntityComponentTypes.ITEM_RENDER.get())
                                            .setLocation(new ResourceLocation("apple"));
                                    a.addComponent(EntityComponentTypes.ITEM_EATEN.get())
                                            .setFillAmount(2)
                                            .setDuration(16)
                                            .setSaturation(0.3F)
                                            .setPotionEffectList(Lists.newArrayList(new EffectInstance(Effects.POISON, 200, 0)));
                                }))
                ));

        this.baseAttacher.addComponent(EntityComponentTypes.BLOCK_PLACEABLE.get(), EntityStorageOverrides.PLANT_PLACEABLE)
                .setPlantType(PlantType.PLAINS);

        this.attachProperty("age")
                .attachOverride("0", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("1");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT.get()).setPotionEffectList(Lists.newArrayList(new EffectInstance(Effects.POISON, 100, 0)));
                })
                .attachOverride("1", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("2");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT.get()).setPotionEffectList(Lists.newArrayList(new EffectInstance(Effects.POISON, 120, 0)));
                })
                .attachOverride("2", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_GROWING.get()).setGrowTo("3");
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT.get()).setPotionEffectList(Lists.newArrayList(new EffectInstance(Effects.POISON, 160, 1)));
                })
                .attachOverride("3", a -> {
                    a.addComponent(EntityComponentTypes.BLOCK_TOUCH_EFFECT.get()).setPotionEffectList(Lists.newArrayList(new EffectInstance(Effects.POISON, 180, 1)));
                });
    }
}
