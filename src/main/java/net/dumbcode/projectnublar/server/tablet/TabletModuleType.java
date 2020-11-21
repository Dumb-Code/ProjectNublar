package net.dumbcode.projectnublar.server.tablet;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Getter;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Builder
public class TabletModuleType<S extends TabletModuleStorage> extends IForgeRegistryEntry.Impl<TabletModuleType<?>> {
    @Builder.Default
    private final Supplier<S> storageCreator = () -> null;
    private final Function<ByteBuf, TabletPage> screenCreator;
    @Builder.Default
    private final TriConsumer<S, EntityPlayerMP, ByteBuf> screenData = (s, p, b) -> {};

    @SuppressWarnings("unchecked")
    public static Class<TabletModuleType<?>> getWildcardType() {
        return (Class<TabletModuleType<?>>) (Class<?>) TabletModuleType.class;
    }
}
