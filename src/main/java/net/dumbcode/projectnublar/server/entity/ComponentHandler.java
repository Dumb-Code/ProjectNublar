package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.impl.*;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackFenceComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.DefenseComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.dumbcode.projectnublar.server.registry.EarlyDeferredRegister;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import static net.dumbcode.dumblibrary.server.ecs.component.SimpleComponentType.of;

public class ComponentHandler {

    public static final EarlyDeferredRegister<EntityComponentType<? ,?>> REGISTER = EarlyDeferredRegister.wrap(DeferredRegister.create(EntityComponentType.getWildcardType(), ProjectNublar.MODID));

    public static final RegistryObject<EntityComponentType<DinosaurComponent,?>> DINOSAUR = REGISTER.register("dinosaur", () -> of(DinosaurComponent.class, DinosaurComponent::new));
    public static final RegistryObject<EntityComponentType<DinosaurCompatComponent,?>> DINOSAUR_COMPAT = REGISTER.register("dinosaur_compat", () -> of(DinosaurCompatComponent.class, DinosaurCompatComponent::new));
    public static final RegistryObject<EntityComponentType<AgeComponent,AgeComponent.Storage>> AGE = REGISTER.register("age", () -> of(AgeComponent.class, AgeComponent::new, AgeComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<MultipartEntityComponent,?>> MULTIPART = REGISTER.register("multipart", () -> of(MultipartEntityComponent.class, MultipartEntityComponent::new));
    public static final RegistryObject<EntityComponentType<WanderComponent,?>> WANDER_AI = REGISTER.register("wander_ai", () -> of(WanderComponent.class, WanderComponent::new));
    public static final RegistryObject<EntityComponentType<MoodComponent, ?>> MOOD = REGISTER.register("mood", () -> of(MoodComponent.class, MoodComponent::new));
    public static final RegistryObject<EntityComponentType<AttackFenceComponent, ?>> ATTACK_FENCE_AI = REGISTER.register("attack_fence_ai", () -> of(AttackFenceComponent.class, AttackFenceComponent::new));
    public static final RegistryObject<EntityComponentType<MetabolismComponent, MetabolismComponent.Storage>> METABOLISM = REGISTER.register("metabolism", () -> of(MetabolismComponent.class, MetabolismComponent::new, MetabolismComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<SkeletalBuilderComponent, SkeletalBuilderComponent.Storage>> SKELETAL_BUILDER = REGISTER.register("skeletal_builder", () -> of(SkeletalBuilderComponent.class, SkeletalBuilderComponent::new, SkeletalBuilderComponent.Storage::new, false));
    public static final RegistryObject<EntityComponentType<DinosaurDropsComponent, DinosaurDropsComponent.Storage>> ITEM_DROPS = REGISTER.register("item_drops", () -> of(DinosaurDropsComponent.class, DinosaurDropsComponent::new, DinosaurDropsComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<AttackComponent, ?>> ATTACK_AI = REGISTER.register("attack_ai", () -> of(AttackComponent.class, AttackComponent::new));
    public static final RegistryObject<EntityComponentType<DinosaurEggLayingComponent, DinosaurEggLayingComponent.Storage>> DINOSAUR_EGG_LAYING = REGISTER.register("dinosaur_egg_laying", () -> of(DinosaurEggLayingComponent.class, DinosaurEggLayingComponent::new, DinosaurEggLayingComponent.Storage::new));
    public static final RegistryObject<EntityComponentType<TrackingComponent, ?>> TRACKING_DATA = REGISTER.register("tracking_data", () -> of(TrackingComponent.class, TrackingComponent::new));
    public static final RegistryObject<EntityComponentType<BasicEntityInformationComponent, ?>> BASIC_ENTITY_INFORMATION = REGISTER.register("basic_entity_information", () -> of(BasicEntityInformationComponent.class, BasicEntityInformationComponent::new));
    public static final RegistryObject<EntityComponentType<DefenseComponent, DefenseComponent.Storage>> DEFENSE = REGISTER.register("defense", () -> of(DefenseComponent.class, DefenseComponent::new, DefenseComponent.Storage::new));
}

