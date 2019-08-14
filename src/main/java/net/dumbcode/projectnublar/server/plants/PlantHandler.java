package net.dumbcode.projectnublar.server.plants;

import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.registry.RegisterPlantEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class PlantHandler {

    public static final Plant CYCAD = InjectedUtils.injected();
    public static final Plant SERENNA_VERIFORMANS = InjectedUtils.injected();

    @SubscribeEvent
    public static void register(RegisterPlantEvent event) {
        event.getRegistry().registerAll(
                new Cycad().setRegistryName("cycad"),
                new SerennaVeriformans().setRegistryName("serenna_veriformans")

        );
    }
}
