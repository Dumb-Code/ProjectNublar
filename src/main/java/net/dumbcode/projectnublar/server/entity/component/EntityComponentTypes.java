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
    public static final EntityComponentType<DinosaurComponent> DINOSAUR = InjectedUtils.injected();
    public static final EntityComponentType<GenderComponent> GENDER = InjectedUtils.injected();
    public static final EntityComponentType<AgeComponent> AGE = InjectedUtils.injected();
    public static final EntityComponentType<HerdComponent> HERD = InjectedUtils.injected();
    public static final EntityComponentType<AdvancedAIComponent> ADVANCED_AI = InjectedUtils.injected();
    public static final EntityComponentType<MetabolismComponent> METABOLISM = InjectedUtils.injected();
    public static final EntityComponentType<MultipartEntityComponent> MULTIPART = InjectedUtils.injected();

    public static final EntityComponentType<WanderComponent> WANDER_AI = InjectedUtils.injected();

    public static final EntityComponentType<AnimationComponent> ANIMATION = InjectedUtils.injected();

    public static final EntityComponentType<SkeletalBuilderCompoent> SKELETAL_BUILDER = InjectedUtils.injected();

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
                SimpleComponentType.builder(AgeComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "age"))
                        .withConstructor(AgeComponent::new)
                        .build(),
                SimpleComponentType.builder(HerdComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "herd"))
                        .withConstructor(HerdComponent::new)
                        .build(),
                SimpleComponentType.builder(AdvancedAIComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "advanced_ai"))
                        .withConstructor(AdvancedAIComponent::new)
                        .build(),
                SimpleComponentType.builder(MetabolismComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "metabolism"))
                        .withConstructor(MetabolismComponent::new)
                        .build(),
                SimpleComponentType.builder(MultipartEntityComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "multipart"))
                        .withConstructor(MultipartEntityComponent::new)
                        .build(),
                SimpleComponentType.builder(WanderComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "wander_ai"))
                        .withConstructor(WanderComponent::new)
                        .build(),
                SimpleComponentType.builder(SkeletalBuilderCompoent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "skeletal_builder"))
                        .withConstructor(SkeletalBuilderCompoent::new)
                        .build(),
                SimpleComponentType.builder(AnimationComponent.class)
                        .withIdentifier(new ResourceLocation(ProjectNublar.MODID, "animation"))
                        .withConstructor(AnimationComponent::new)
                        .build()
        );
    }
}
