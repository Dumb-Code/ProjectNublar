package net.dumbcode.projectnublar.server.tablet;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.server.network.C28ModuleClicked;
import net.dumbcode.projectnublar.server.network.S29OpenTabletModule;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.concurrent.Callable;
import java.util.function.*;

@Getter
@Builder
public class TabletModuleType<S extends TabletModuleStorage> extends IForgeRegistryEntry.Impl<TabletModuleType<?>> {
    @Builder.Default
    private final Supplier<S> storageCreator = () -> null;
    private final Function<ByteBuf, TabletScreen> screenCreator;
    @Builder.Default
    private final TriConsumer<S, EntityPlayerMP, ByteBuf> screenData = (s, p, b) -> {};

    @SuppressWarnings("unchecked")
    public static Class<TabletModuleType<?>> getWildcardType() {
        return (Class<TabletModuleType<?>>) (Class<?>) TabletModuleType.class;
    }
}
