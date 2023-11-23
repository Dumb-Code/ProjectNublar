package net.dumbcode.projectnublar.server.tablet;

import lombok.Builder;
import lombok.Getter;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Builder
public class TabletModuleType<S extends TabletModuleStorage> {
    ResourceLocation registryName;
    @Builder.Default
    private final Supplier<S> storageCreator = () -> null;
    private final Function<FriendlyByteBuf, TabletScreen> screenCreator;
    @Builder.Default
    private final TriConsumer<S, ServerPlayer, FriendlyByteBuf> screenData = (s, p, b) -> {};

    @SuppressWarnings("unchecked")
    public static Class<TabletModuleType<?>> getWildcardType() {
        return (Class<TabletModuleType<?>>) (Class<?>) TabletModuleType.class;
    }

    public void setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
    }
}
