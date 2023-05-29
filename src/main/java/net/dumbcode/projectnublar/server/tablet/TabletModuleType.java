package net.dumbcode.projectnublar.server.tablet;

import lombok.Builder;
import lombok.Getter;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Builder
public class TabletModuleType<S extends TabletModuleStorage> extends ForgeRegistryEntry<TabletModuleType<?>> {
    @Builder.Default
    private final Supplier<S> storageCreator = () -> null;
    private final Function<PacketBuffer, TabletPage> screenCreator;
    @Builder.Default
    private final TriConsumer<S, ServerPlayerEntity, PacketBuffer> screenData = (s, p, b) -> {};

    @SuppressWarnings("unchecked")
    public static Class<TabletModuleType<?>> getWildcardType() {
        return (Class<TabletModuleType<?>>) (Class<?>) TabletModuleType.class;
    }
}
