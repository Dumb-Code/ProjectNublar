package net.dumbcode.projectnublar.server.dinosaur;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.registry.RegisterDinosaurEvent;
import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class DinosaurHandler {

    public static final Dinosaur TYRANNOSAURUS = InjectedUtils.injected();

    @SubscribeEvent
    public static void register(RegisterDinosaurEvent event) {
        event.getRegistry().register(new Tyrannosaurus().setRegistryName("projectnublar:tyrannosaurus"));
    }

}
