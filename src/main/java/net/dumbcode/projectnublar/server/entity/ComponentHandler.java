package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.RegisterComponentsEvent;
import net.dumbcode.dumblibrary.server.ecs.component.SimpleComponentType;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.impl.*;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.dumbcode.projectnublar.server.plants.component.PlantComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class ComponentHandler {

    public static final EntityComponentType<DinosaurComponent,?> DINOSAUR = InjectedUtils.injected();
    public static final EntityComponentType<AgeComponent,AgeComponent.Storage> AGE = InjectedUtils.injected();
    public static final EntityComponentType<MultipartEntityComponent,?> MULTIPART = InjectedUtils.injected();
    public static final EntityComponentType<WanderComponent,?> WANDER_AI = InjectedUtils.injected();
    public static final EntityComponentType<WanderComponent,?> ATTACK_AI = InjectedUtils.injected();
    public static final EntityComponentType<SkeletalBuilderComponent, SkeletalBuilderComponent.Storage> SKELETAL_BUILDER = InjectedUtils.injected();

    public static final EntityComponentType<DinosaurDropsComponent, DinosaurDropsComponent.Storage> ITEM_DROPS = InjectedUtils.injected();


    public static final EntityComponentType<PlantComponent,?> PLANT = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterComponents(RegisterComponentsEvent event) {
        event.getRegistry().registerAll(
                SimpleComponentType.builder(DinosaurComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                        .withConstructor(DinosaurComponent::new)
                        .build(),
                SimpleComponentType.builder(AgeComponent.class, AgeComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "age"))
                        .withConstructor(AgeComponent::new)
                        .withStorage(AgeComponent.Storage::new)
                        .build(),
                SimpleComponentType.builder(MultipartEntityComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "multipart"))
                        .withConstructor(MultipartEntityComponent::new)
                        .build(),
                SimpleComponentType.builder(WanderComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "wander_ai"))
                        .withConstructor(WanderComponent::new)
                        .build(),
                SimpleComponentType.builder(SkeletalBuilderComponent.class, SkeletalBuilderComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "skeletal_builder"))
                        .withConstructor(SkeletalBuilderComponent::new)
                        .withStorage(SkeletalBuilderComponent.Storage::new)
                        .disableDefaultAttach()
                        .build(),
                SimpleComponentType.builder(DinosaurDropsComponent.class, DinosaurDropsComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "item_drops"))
                        .withConstructor(DinosaurDropsComponent::new)
                        .withStorage(DinosaurDropsComponent.Storage::new)
                        .build(),
                SimpleComponentType.builder(AttackComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "attack_ai"))
                        .withConstructor(AttackComponent::new)
                        .build()
        );
    }
}

