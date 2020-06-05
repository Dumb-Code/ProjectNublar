package net.dumbcode.projectnublar.server.entity.tracking;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.IntStream;

public class TrackingSavedData extends WorldSavedData {

    private final Set<DataEntry> entries = new HashSet<>();

    public TrackingSavedData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.entries.clear();
        StreamUtils.stream(nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND)).map(b -> DataEntry.deserialize((NBTTagCompound) b)).forEach(this.entries::add);
    }

    public Set<DataEntry> getEntries() {
        return Collections.unmodifiableSet(this.entries);
    }

    public void setEntry(DataEntry entry) {
        this.removeEntry(entry.uuid);
        this.entries.add(entry);
        this.markDirty();
    }

    public void removeEntry(UUID uuid) {
        this.entries.remove(new DataEntry(uuid, Vec3d.ZERO));
        this.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("entries", this.entries.stream().map(DataEntry::serialize).collect(CollectorUtils.toNBTTagList()));
        return nbt;
    }

    public static TrackingSavedData getData(World world) {
        String identifier = "tracking_data";
        TrackingSavedData data = (TrackingSavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(TrackingSavedData.class, identifier);
        if(data == null) {
            data = new TrackingSavedData(identifier);
            world.getMapStorage().setData(identifier, data);
        }
        return data;
    }

    //Per entity
    @Value
    public static class DataEntry {
        private final UUID uuid;
        @EqualsAndHashCode.Exclude
        private final Vec3d position;
        @EqualsAndHashCode.Exclude
        private final List<TrackingDataInformation> information = new ArrayList<>();

        public static NBTTagCompound serialize(DataEntry info) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setUniqueId("uuid", info.uuid);
            compound.setDouble("position_x", info.position.x);
            compound.setDouble("position_y", info.position.y);
            compound.setDouble("position_z", info.position.z);
            compound.setTag("infos", info.information.stream().map(d -> TrackingDataInformation.serializeNBT(new NBTTagCompound(), d)).collect(CollectorUtils.toNBTTagList()));
            return compound;
        }

        public static DataEntry deserialize(NBTTagCompound nbt) {
            DataEntry info = new DataEntry(nbt.getUniqueId("uuid"), new Vec3d(nbt.getDouble("position_x"), nbt.getDouble("position_y"), nbt.getDouble("position_z")));
            StreamUtils.stream(nbt.getTagList("infos", Constants.NBT.TAG_COMPOUND))
                .map(base -> TrackingDataInformation.deserializeNBT((NBTTagCompound) base))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(info.information::add);

            return info;
        }

        public static void serialize(ByteBuf buf, DataEntry info) {
            buf.writeLong(info.uuid.getLeastSignificantBits());
            buf.writeLong(info.uuid.getMostSignificantBits());

            buf.writeDouble(info.position.x);
            buf.writeDouble(info.position.y);
            buf.writeDouble(info.position.z);

            buf.writeShort(info.information.size());
            info.information.forEach(d -> TrackingDataInformation.serializeBuf(buf, d));
        }

        public static DataEntry deserailize(ByteBuf buf) {
            DataEntry info = new DataEntry(new UUID(buf.readLong(), buf.readLong()),new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            IntStream.range(0, buf.readShort())
                .mapToObj(i -> TrackingDataInformation.deserializeBuf(buf))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(info.information::add);
            return info;
        }

    }
}
