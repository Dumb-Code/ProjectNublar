package net.dumbcode.projectnublar.server.entity.component;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class EntityComponentTypes {
    @SubscribeEvent
    public static void onRegisterComponents(RegisterComponentsEvent event) {
    }
}
