package net.dumbcode.projectnublar.server.entity.component;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.impl.*;
import net.dumbcode.projectnublar.server.registry.RegisterComponentsEvent;
import net.dumbcode.projectnublar.server.utils.InjectedUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class EntityComponentTypes {
    public static final EntityComponentType<DinosaurComponent,?> DINOSAUR = InjectedUtils.injected();
    public static final EntityComponentType<GenderComponent,?> GENDER = InjectedUtils.injected();
    public static final EntityComponentType<AgeComponent,AgeComponent.Storage> AGE = InjectedUtils.injected();
    public static final EntityComponentType<HerdComponent,HerdComponent.Storage> HERD = InjectedUtils.injected();
    public static final EntityComponentType<AdvancedAIComponent,?> ADVANCED_AI = InjectedUtils.injected();
    public static final EntityComponentType<MetabolismComponent,MetabolismComponent.Storage> METABOLISM = InjectedUtils.injected();
    public static final EntityComponentType<MultipartEntityComponent,MultipartEntityComponent.Storage> MULTIPART = InjectedUtils.injected();

    public static final EntityComponentType<WanderComponent,?> WANDER_AI = InjectedUtils.injected();

    public static final EntityComponentType<AnimationComponent, AnimationComponent.Storage> ANIMATION = InjectedUtils.injected();

    public static final EntityComponentType<SkeletalBuilderComponent, SkeletalBuilderComponent.Storage> SKELETAL_BUILDER = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterComponents(RegisterComponentsEvent event) {
        event.getRegistry().registerAll(
                SimpleComponentType.builder(DinosaurComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "dinosaur"))
                        .withConstructor(DinosaurComponent::new)
                        .build(),
                SimpleComponentType.builder(GenderComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "gender"))
                        .withConstructor(GenderComponent::new)
                        .build(),
                SimpleComponentType.builder(AgeComponent.class, AgeComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "age"))
                        .withStorage(AgeComponent.Storage::new)
                        .withConstructor(AgeComponent::new)
                        .build(),
                SimpleComponentType.builder(HerdComponent.class, HerdComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "herd"))
                        .withStorage(HerdComponent.Storage::new)
                        .withConstructor(HerdComponent::new)
                        .build(),
                SimpleComponentType.builder(AdvancedAIComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "advanced_ai"))
                        .withConstructor(AdvancedAIComponent::new)
                        .build(),
                SimpleComponentType.builder(MetabolismComponent.class, MetabolismComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "metabolism"))
                        .withStorage(MetabolismComponent.Storage::new)
                        .withConstructor(MetabolismComponent::new)
                        .build(),
                SimpleComponentType.builder(MultipartEntityComponent.class, MultipartEntityComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "multipart"))
                        .withStorage(MultipartEntityComponent.Storage::new)
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
                SimpleComponentType.builder(AnimationComponent.class, AnimationComponent.Storage.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "animation"))
                        .withConstructor(AnimationComponent::new)
                        .withStorage(AnimationComponent.Storage::new)
                        .build()
        );
    }
}
