package net.dumbcode.projectnublar.server.dna;

import net.dumbcode.dumblibrary.server.dna.GeneticFieldModifierStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.dumbcode.dumblibrary.server.registry.RegisterGeneticTypes;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.AttackComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.DefenseComponent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class GeneticHandler {

    public static final GeneticType<GeneticFieldModifierStorage> STRENGTH = InjectedUtils.injected();
    public static final GeneticType<GeneticFieldModifierStorage> DEFENSE = InjectedUtils.injected();

    @SubscribeEvent
    public static void onRegisterGenetics(RegisterGeneticTypes event) {
        event.getRegistry().registerAll(
            GeneticType.simpleFieldModifierType(ComponentHandler.ATTACK_AI, AttackComponent::getAttackDamage, "strength"),
            GeneticType.simpleFieldModifierType(ComponentHandler.DEFENSE, DefenseComponent::getDefense, "defense")
        );
    }

}
