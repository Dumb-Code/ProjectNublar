package net.dumbcode.projectnublar.server.dinosaur;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.registry.RegisterDinosaurEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class DinosaurHandler {

    public static final Dinosaur TYRANNOSAURUS = new Tyrannosaurus().setRegistryName("projectnublar:tyrannosaurus");

    @SubscribeEvent
    public static void register(RegisterDinosaurEvent event) {
        event.getRegistry().register(new Tyrannosaurus().setRegistryName("projectnublar:missing")); // TODO: custom class?
        event.getRegistry().register(TYRANNOSAURUS);
    }

}
