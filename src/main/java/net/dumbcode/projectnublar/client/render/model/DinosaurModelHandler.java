package net.dumbcode.projectnublar.client.render.model;

import com.google.common.collect.Maps;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.client.animation.objects.AnimationPass;
import net.dumbcode.dumblibrary.client.animation.objects.EntityAnimator;
import net.dumbcode.projectnublar.client.render.dinosaur.DinosaurAnimations;
import net.dumbcode.projectnublar.client.render.dinosaur.objects.MovementAnimationPass;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.item.StackModelVarient;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class DinosaurModelHandler {

    private static Map<Item, DinosaurItemModel> itemMap = Maps.newHashMap();

    static IBakedModel MISSING_MODEL;

    @SubscribeEvent
    public static void onModelReady(ModelRegistryEvent event) {
        for (Dinosaur dinosaur : ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection()) {
            dinosaur.setModelContainer(new ModelContainer(dinosaur.getRegName(),
                    dinosaur.getModelProperties().getModelGrowthStages(), dinosaur.getModelProperties().getMainModelMap(),
                    DinosaurAnimations.getNames(), dinosaur.getModelProperties().getEntityAnimatorSupplier(),
                    s -> DinosaurAnimations.fromName(s).get(), DinosaurAnimations.IDLE.get(),
                    DinosaurAnimations::getAnimation,
                    AnimationPass::new, MovementAnimationPass::new));
        }
    }

    @SubscribeEvent
    public static void onModelsBaked(ModelBakeEvent event) {
        MISSING_MODEL = event.getModelManager().getMissingModel();
        IRegistry<ModelResourceLocation, IBakedModel> map = event.getModelRegistry();
        itemMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().shouldRegister())
                .forEach(entry -> map.putObject(new ModelResourceLocation(entry.getKey().getRegistryName(), "inventory"), entry.getValue()));
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onTextureStitched(TextureStitchEvent event) {
        for(Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            StackModelVarient provider = StackModelVarient.getFromStack(new ItemStack(item));
            if(provider == null) {
                continue;
            }
            itemMap.put(item, new DinosaurItemModel(provider, event.getMap()));
        }
    }}
