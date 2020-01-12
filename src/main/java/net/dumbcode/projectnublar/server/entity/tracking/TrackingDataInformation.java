package net.dumbcode.projectnublar.server.entity.tracking;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.dumbcode.projectnublar.server.entity.tracking.info.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TrackingDataInformation {

    private static final Map<String, Entry> REGISTERED_MAP = new HashMap<>();

    private final String typeName = this.getTypeName();

    protected abstract String getTypeName();

    public void renderMap(int x, int y) {

    }

    @Nonnull
    public Dimension getInfoDimensions() {
        return new Dimension();
    }

    public void renderInfo(int x, int y, int relativeMouseX, int relativeMouseY) {

    }

    public static <T extends TrackingDataInformation> void registerTrackingType(
        String typeName,
        Function<ByteBuf, T> bufDeserailizer, BiConsumer<ByteBuf, T> bufSerializer,
        Function<NBTTagCompound, T> nbtDeserailizer, BiConsumer<NBTTagCompound, T> nbtSerailizer
    ) {
        REGISTERED_MAP.put(typeName, new Entry<>(bufDeserailizer, bufSerializer, nbtDeserailizer, nbtSerailizer));
    }

    private static Entry<TrackingDataInformation> getEntry(String name) {
        @SuppressWarnings("unchecked")
        Entry<TrackingDataInformation> entry = REGISTERED_MAP.get(name);
        if(entry == null) {
            throw new NullPointerException("Could not find type with name " + name);
        }
        return entry;
    }

    public static TrackingDataInformation deseralizeBuf(ByteBuf buf) {
        return getEntry(ByteBufUtils.readUTF8String(buf)).bufDeserailizer.apply(buf);
    }

    public static void seralizeBuf(ByteBuf buf, TrackingDataInformation info) {
        ByteBufUtils.writeUTF8String(buf, info.typeName);
        getEntry(info.typeName).bufSerializer.accept(buf, info);
    }

    public static TrackingDataInformation deseralizeNBT(NBTTagCompound nbt) {
        return getEntry(nbt.getString("key")).nbtDeserailizer.apply(nbt);
    }

    public static NBTTagCompound seralizeNBT(NBTTagCompound nbt, TrackingDataInformation info) {
        nbt.setString("key", info.typeName);
        getEntry(info.typeName).nbtSerailizer.accept(nbt, info);
        return nbt;
    }

    @Value
    private static class Entry<T extends TrackingDataInformation> {
        private final Function<ByteBuf, T> bufDeserailizer;
        private final BiConsumer<ByteBuf, T> bufSerializer;
        private final Function<NBTTagCompound, T> nbtDeserailizer;
        private final BiConsumer<NBTTagCompound, T> nbtSerailizer;
    }

    static {
        registerTrackingType(BasicEntityInformation.KEY, BasicEntityInformation::decodeBuf, BasicEntityInformation::encodeBuf, BasicEntityInformation::decodeNBT, BasicEntityInformation::encodeNBT);
        registerTrackingType(DinosaurInformation.KEY, DinosaurInformation::decodeBuf, DinosaurInformation::encodeBuf, DinosaurInformation::decodeNBT, DinosaurInformation::encodeNBT);
        registerTrackingType(PregnancyInformation.KEY, PregnancyInformation::decodeBuf, PregnancyInformation::encodeBuf, PregnancyInformation::decodeNBT, PregnancyInformation::encodeNBT);
        registerTrackingType(MoodInformation.KEY, MoodInformation::decodeBuf, MoodInformation::encodeBuf, MoodInformation::decodeNBT, MoodInformation::encodeNBT);
        registerTrackingType(MetabolismInformation.KEY, MetabolismInformation::decodeBuf, MetabolismInformation::encodeBuf, MetabolismInformation::decodeNBT, MetabolismInformation::encodeNBT);
    }
}
