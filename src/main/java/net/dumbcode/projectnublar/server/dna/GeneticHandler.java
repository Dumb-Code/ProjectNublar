package net.dumbcode.projectnublar.server.dna;

import net.dumbcode.dumblibrary.server.dna.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.DefenseComponent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class GeneticHandler {

    public static final DeferredRegister<GeneticType<?>> REGISTER = DeferredRegister.create(GeneticType.getWildcardType(), ProjectNublar.MODID);

    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> STRENGTH = REGISTER.register("strength", () -> GeneticType.simpleFieldModifierType(ComponentHandler.ATTACK_AI.get(), AttackComponent::getAttackDamage));
    public static final RegistryObject<GeneticType<GeneticFieldModifierStorage>> DEFENSE = REGISTER.register("defense", () -> GeneticType.simpleFieldModifierType(ComponentHandler.DEFENSE.get(), DefenseComponent::getDefense));

}
