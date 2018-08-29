package net.dumbcode.projectnublar.server.entity.component;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class EntityComponentTypes {
    @SubscribeEvent
    public static void onRegisterComponents(RegisterComponentEvent event) {
    }
}
