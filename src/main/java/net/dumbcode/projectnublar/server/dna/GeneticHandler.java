package net.dumbcode.projectnublar.server.dna;

import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dna.storages.GeneticDietChangeStorage;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.DefenseComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class GeneticHandler {

    public static final DeferredRegister<GeneticType<?>> REGISTER = DeferredRegister.create(GeneticType.getWildcardType(), ProjectNublar.MODID);

    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> STRENGTH = REGISTER.register("strength", () -> GeneticType.simpleFieldModifierType(ComponentHandler.ATTACK_AI.get(), AttackComponent::getAttackDamage));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> DEFENSE = REGISTER.register("defense", () -> GeneticType.simpleFieldModifierType(ComponentHandler.DEFENSE.get(), DefenseComponent::getDefense));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> EAT_RATE = REGISTER.register("hunger", () -> GeneticType.simpleFieldModifierType(ComponentHandler.METABOLISM.get(), MetabolismComponent::getFoodRate));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> STOMACH_CAPACITY = REGISTER.register("stomach_capacity", () -> GeneticType.simpleFieldModifierType(ComponentHandler.METABOLISM.get(), MetabolismComponent::getMaxFood));

    public static final RegistryObject<GeneticType<GeneticDietChangeStorage>> DIET_CHANGE = REGISTER.register("diet_change", () ->
        GeneticType.<GeneticDietChangeStorage>builder()
            .storage(GeneticDietChangeStorage::new)
            .onChange((value, type, storage) -> type.get(ComponentHandler.METABOLISM).ifPresent(m -> m.addGeneticDiet(storage.getRandomUUID(), storage.getDiet())))
            .build()

    );

    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> HUNTING_PACK_SIZE = REGISTER.register("hunting_pack_size", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> AGGRESSION = REGISTER.register("aggression", GeneticType::unfinished);
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> HERD_SIZE = REGISTER.register("herd_size", GeneticType::unfinished);


}
