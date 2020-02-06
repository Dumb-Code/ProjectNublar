package net.dumbcode.projectnublar.server.tablet;

import net.dumbcode.dumblibrary.server.utils.InjectedUtils;
import net.dumbcode.projectnublar.client.gui.tablet.screens.FlappyDinoScreen;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@GameRegistry.ObjectHolder(ProjectNublar.MODID)
public class TabletModuleHandler {

    public static final TabletModuleType<?> TRACKING_TABLET = InjectedUtils.injected();
    public static final TabletModuleType<?> FLAPPY_DINO = InjectedUtils.injected();

    @SubscribeEvent
    public static void onTabletRegistry(RegistryEvent.Register event) {
        if(event.getRegistry().getRegistrySuperType() == TabletModuleType.getWildcardType()) {
            @SuppressWarnings("unchecked")
            IForgeRegistry<TabletModuleType<?>> registry = (IForgeRegistry<TabletModuleType<?>>) event.getRegistry();
            registry.registerAll(

                TabletModuleType.builder()
                    .screenCreator(buf -> new TrackingTabletScreen(
                            IntStream.range(0, buf.readShort())
                                .mapToObj(i -> Pair.of(BlockPos.fromLong(buf.readLong()), ByteBufUtils.readUTF8String(buf)))
                                .collect(Collectors.toList())
                        )
                    )
                    .screenData((s, player, buf) -> {
                        List<TrackingBeaconBlockEntity.TrackingSavedDataEntry> entries = TrackingBeaconBlockEntity.getTrackingList(player.world).getList();
                        buf.writeShort(entries.size());
                        for (TrackingBeaconBlockEntity.TrackingSavedDataEntry entry : entries) {
                            buf.writeLong(entry.getPos().toLong());
                            ByteBufUtils.writeUTF8String(buf, entry.getName());
                        }
                    })//new TrackingTabletIterator(player, player.getPosition(), 500).writeData(buf)
                    .build().setRegistryName("tracking_tablet"),

                TabletModuleType.builder()
                    .screenCreator(b -> new FlappyDinoScreen())
                    .build().setRegistryName("flappy_dino")

            );
        }
    }
}
