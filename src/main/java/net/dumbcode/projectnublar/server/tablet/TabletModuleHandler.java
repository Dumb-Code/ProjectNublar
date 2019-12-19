package net.dumbcode.projectnublar.server.tablet;

import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class TabletModuleHandler {

    public static final TabletModuleType<?> TRACKING_TABLET = InjectedUtils.injected();

    @SubscribeEvent
    public static void onTabletRegistry(RegistryEvent.Register event) {
        if(event.getRegistry().getRegistrySuperType() == TabletModuleType.getWildcardType()) {
            @SuppressWarnings("unchecked")
            IForgeRegistry<TabletModuleType<?>> registry = (IForgeRegistry<TabletModuleType<?>>) event.getRegistry();
            registry.registerAll(
                new TabletModuleType<>(() -> null).setRegistryName("tracking_tablet")
            );
        }
    }
}
