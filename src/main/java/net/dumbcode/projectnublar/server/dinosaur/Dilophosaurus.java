package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.entity.objects.FeedingDiet;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurPeriod;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.NublarEntityComponentTypes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;
import java.util.Map;

public class Dilophosaurus extends Dinosaur {

    public Dilophosaurus() {
        this.getScale().put(ModelStage.ADULT, 1D);

        getItemProperties()
                .setCookedMeatHealAmount(5)
                .setCookedMeatSaturation(1f)
                .setRawMeatHealAmount(4)
                .setRawMeatSaturation(0.6f)
                .setCookingExperience(1f);

        DinosaurInformation dinosaurInfomation = this.getDinosaurInfomation();
        dinosaurInfomation.setPeriod(DinosaurPeriod.JURASSIC);
        dinosaurInfomation.getBiomeTypes().addAll(Lists.newArrayList(
                BiomeDictionary.Type.CONIFEROUS,
                BiomeDictionary.Type.PLAINS,
                BiomeDictionary.Type.FOREST
        ));

        this.getActiveModels().addAll(Lists.newArrayList(ModelStage.ADULT, ModelStage.SKELETON));
    }

    @Override
    public void attachDefaultComponents() {
        addComponent(EntityComponentTypes.METABOLISM)
                .setDistanceSmellFood(30)
                .setDiet(new FeedingDiet()
                        .add(new ItemStack(Items.PORKCHOP))
                        .add(new ItemStack(Items.COOKED_PORKCHOP))
                        .add(new ItemStack(Items.CHICKEN))
                        .add(new ItemStack(Items.COOKED_CHICKEN))
                        .add(new ItemStack(Items.BEEF))
                        .add(new ItemStack(Items.COOKED_BEEF)))
                .setMaxFood(6000)
                .setMaxWater(4500);

        Map<ModelStage, List<String>> entity = Maps.newEnumMap(ModelStage.class);
        entity.put(ModelStage.ADULT, Lists.newArrayList(
                "tail4",
                "tail3",
                "tail2",
                "tail1",

                "legUpperRight",
                "legMiddleRight",
                "legLowerRight",

                "legUpperLeft",
                "legMiddleLeft",
                "legLowerLeft",

                "neck2",
                "neck3",

                "hips",
                "chest",
                "jawUpper1",
                "head"));
        this.addComponent(NublarEntityComponentTypes.MULTIPART)
                .setDefaultStage(ModelStage.ADULT)
                .setLinkedCubeMap(entity);
        this.addComponent(EntityComponentTypes.ANIMATION)
                .setModelGetter(new DinosaurModelGetter(this));
        this.addComponent(EntityComponentTypes.GENDER);
        this.addComponent(NublarEntityComponentTypes.AGE);
        this.addComponent(EntityComponentTypes.HERD)
                .setHerdTypeID(new ResourceLocation(ProjectNublar.MODID, "dinosaur_herd_" + this.getFormattedName()));
        this.addComponent(NublarEntityComponentTypes.WANDER_AI);
        this.addComponent(NublarEntityComponentTypes.SKELETAL_BUILDER)
                .initializeMap(
                        "foot", "legLowerLeft",
                        "foot", "legLowerRight",
                        "leg", "legUpperLeft",
                        "leg", "legUpperRight",
                        "body", "hips"
                );
    }
}
