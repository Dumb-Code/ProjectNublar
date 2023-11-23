package net.dumbcode.projectnublar.server.entity.tracking;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.info.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class TrackingDataInformation {

    private static final Map<String, Entry> REGISTERED_MAP = new HashMap<>();

    private final String typeName = this.getTypeName();

    protected abstract String getTypeName();

    public void renderMap(GuiGraphics stack, int x, int y) {

    }

    @Nonnull
    public Dimension getInfoDimensions() {
        return new Dimension();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderInfo(GuiGraphics stack, int x, int y, int relativeMouseX, int relativeMouseY) {

    }

    public static <T extends TrackingDataInformation> void registerTrackingType(
        String typeName,
        Function<FriendlyByteBuf, T> bufDeserailizer, BiConsumer<FriendlyByteBuf, T> bufSerializer,
        Function<CompoundTag, T> nbtDeserailizer, BiConsumer<CompoundTag, T> nbtSerailizer
    ) {
        REGISTERED_MAP.put(typeName, new Entry<>(bufDeserailizer, bufSerializer, nbtDeserailizer, nbtSerailizer));
    }

    @SuppressWarnings("unchecked")
    private static Optional<Entry<TrackingDataInformation>> getEntry(String name) {
        return Optional.ofNullable(REGISTERED_MAP.get(name));
    }

    public static Optional<TrackingDataInformation> deserializeBuf(FriendlyByteBuf buf) {
        return getEntry(buf.readUtf()).map(c -> c.getBufDeserailizer().apply(buf));
    }

    public static void serializeBuf(FriendlyByteBuf buf, TrackingDataInformation info) {
        buf.writeUtf(info.typeName);
        getEntry(info.typeName).ifPresent(c -> c.getBufSerializer().accept(buf, info));
    }

    public static Optional<TrackingDataInformation> deserializeNBT(CompoundTag nbt) {
        return getEntry(nbt.getString("key")).map(c -> c.getNbtDeserailizer().apply(nbt));
    }

    public static CompoundTag serializeNBT(CompoundTag nbt, TrackingDataInformation info) {
        getEntry(info.typeName).ifPresent(c -> {
            nbt.putString("key", info.typeName);
            c.getNbtSerailizer().accept(nbt, info);
        });
        return nbt;
    }

    @Value
    private static class Entry<T extends TrackingDataInformation> {
        Function<FriendlyByteBuf, T> bufDeserailizer;
        BiConsumer<FriendlyByteBuf, T> bufSerializer;
        Function<CompoundTag, T> nbtDeserailizer;
        BiConsumer<CompoundTag, T> nbtSerailizer;
    }

    static {
        registerTrackingType(BasicEntityInformation.KEY, BasicEntityInformation::decodeBuf, BasicEntityInformation::encodeBuf, BasicEntityInformation::decodeNBT, BasicEntityInformation::encodeNBT);
        registerTrackingType(DinosaurInformation.KEY, DinosaurInformation::decodeBuf, DinosaurInformation::encodeBuf, DinosaurInformation::decodeNBT, DinosaurInformation::encodeNBT);
        registerTrackingType(PregnancyInformation.KEY, PregnancyInformation::decodeBuf, PregnancyInformation::encodeBuf, PregnancyInformation::decodeNBT, PregnancyInformation::encodeNBT);
        registerTrackingType(MoodInformation.KEY, MoodInformation::decodeBuf, MoodInformation::encodeBuf, MoodInformation::decodeNBT, MoodInformation::encodeNBT);
        registerTrackingType(MetabolismInformation.KEY, MetabolismInformation::decodeBuf, MetabolismInformation::encodeBuf, MetabolismInformation::decodeNBT, MetabolismInformation::encodeNBT);
    }
}
