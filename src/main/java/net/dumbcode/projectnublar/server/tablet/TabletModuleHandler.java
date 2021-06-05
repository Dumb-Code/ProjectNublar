package net.dumbcode.projectnublar.server.tablet;

import net.dumbcode.projectnublar.client.gui.tablet.screens.FlappyDinoScreen;
import net.dumbcode.projectnublar.client.gui.tablet.screens.TrackingTabletScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TabletModuleHandler {

    public static final DeferredRegister<TabletModuleType<?>> DR = DeferredRegister.create(TabletModuleType.getWildcardType(), ProjectNublar.MODID);

    public static final Supplier<IForgeRegistry<TabletModuleType<?>>> REGISTRY = DR.makeRegistry("tablet_module_type", RegistryBuilder::new);

    public static final RegistryObject<TabletModuleType<?>> TRACKING_TABLET = DR.register("tracking_tablet", () ->
        TabletModuleType.builder()
            .screenCreator(buf -> new TrackingTabletScreen(
                    IntStream.range(0, buf.readShort())
                        .mapToObj(i -> Pair.of(buf.readBlockPos(), buf.readUtf()))
                        .collect(Collectors.toList())
                )
            )
            .screenData((s, player, buf) -> {
                List<TrackingBeaconBlockEntity.TrackingSavedDataEntry> entries = TrackingBeaconBlockEntity.getTrackingList(player.getLevel()).getList();
                buf.writeShort(entries.size());
                for (TrackingBeaconBlockEntity.TrackingSavedDataEntry entry : entries) {
                    buf.writeBlockPos(entry.getPos());
                    buf.writeUtf(entry.getName());
                }
            })
            .build()
    );

    public static final RegistryObject<TabletModuleType<?>> FLAPPY_DINO = DR.register("flappy_dino", () ->
        TabletModuleType.builder()
        .screenCreator(b -> new FlappyDinoScreen())
        .build()
    );

}
