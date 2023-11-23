package net.dumbcode.projectnublar.server.entity.tracking;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.joml.Vector3d;

import java.util.*;
import java.util.stream.IntStream;

public class TrackingSavedData extends WorldSavedData {

    private final Set<DataEntry> entries = new HashSet<>();

    public TrackingSavedData(String name) {
        super(name);
    }

    @Override
    public void load(CompoundTag nbt) {
        this.entries.clear();
        StreamUtils.stream(nbt.getList("entries", Constants.NBT.TAG_COMPOUND)).map(b -> DataEntry.deserialize((CompoundTag) b)).forEach(this.entries::add);
    }

    public Set<DataEntry> getEntries() {
        return Collections.unmodifiableSet(this.entries);
    }

    public void setEntry(DataEntry entry) {
        this.removeEntry(entry.uuid);
        this.entries.add(entry);
        this.setDirty();
    }

    public void removeEntry(UUID uuid) {
        this.entries.remove(new DataEntry(uuid, Vector3d.ZERO));
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("entries", this.entries.stream().map(DataEntry::serialize).collect(CollectorUtils.toNBTTagList()));
        return nbt;
    }

    public static TrackingSavedData getData(ServerWorld world) {
        String identifier = "tracking_data";
        return world.getDataStorage().computeIfAbsent(() -> new TrackingSavedData(identifier), identifier);
    }

    //Per entity
    @Value
    public static class DataEntry {
        UUID uuid;
        @EqualsAndHashCode.Exclude
        Vector3d position;
        @EqualsAndHashCode.Exclude
        List<TrackingDataInformation> information = new ArrayList<>();

        public static CompoundTag serialize(DataEntry info) {
            CompoundTag compound = new CompoundTag();
            compound.putUUID("uuid", info.uuid);
            compound.putDouble("position_x", info.position.x);
            compound.putDouble("position_y", info.position.y);
            compound.putDouble("position_z", info.position.z);
            compound.put("infos", info.information.stream().map(d -> TrackingDataInformation.serializeNBT(new CompoundTag(), d)).collect(CollectorUtils.toNBTTagList()));
            return compound;
        }

        public static DataEntry deserialize(CompoundTag nbt) {
            DataEntry info = new DataEntry(nbt.getUUID("uuid"), new Vector3d(nbt.getDouble("position_x"), nbt.getDouble("position_y"), nbt.getDouble("position_z")));
            StreamUtils.stream(nbt.getList("infos", Constants.NBT.TAG_COMPOUND))
                .map(base -> TrackingDataInformation.deserializeNBT((CompoundTag) base))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(info.information::add);

            return info;
        }

        public static void serialize(FriendlyByteBuf buf, DataEntry info) {
            buf.writeLong(info.uuid.getLeastSignificantBits());
            buf.writeLong(info.uuid.getMostSignificantBits());

            buf.writeDouble(info.position.x);
            buf.writeDouble(info.position.y);
            buf.writeDouble(info.position.z);

            buf.writeShort(info.information.size());
            info.information.forEach(d -> TrackingDataInformation.serializeBuf(buf, d));
        }

        public static DataEntry deserailize(FriendlyByteBuf buf) {
            DataEntry info = new DataEntry(new UUID(buf.readLong(), buf.readLong()),new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> TrackingDataInformation.deserializeBuf(buf))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(info.information::add);
            return info;
        }

    }
}
