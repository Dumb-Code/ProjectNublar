package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackingBeaconBlockEntity extends SimpleBlockEntity {

    @Getter
    private String name = "Unnamed";

    @Getter
    private int radius = 150;

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.name = compound.getString("name");
        this.radius = compound.getInteger("radius");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("name", this.name);
        compound.setInteger("radius", this.radius);
        return super.writeToNBT(compound);
    }

    @Override
    public void onLoad() {
        getTrackingList(this.world).set(this.pos, this.name);
        super.onLoad();
    }

    public void setName(String name) {
        this.name = name;
        this.markDirty();
        this.syncToClient();
        getTrackingList(this.world).set(this.pos, this.name);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        this.markDirty();
        this.syncToClient();
    }

    public static TrackingSavedData getTrackingList(World world) {
        TrackingSavedData data =  (TrackingSavedData) world.getPerWorldStorage().getOrLoadData(TrackingSavedData.class, "tracking_beacon_data");
        if(data == null) {
            world.getPerWorldStorage().setData("tracking_beacon_data", data = new TrackingSavedData("tracking_beacon_data"));
        }
        return data;
    }

    public static class TrackingSavedData extends WorldSavedData {

        private final Map<BlockPos, String> trackingMap = new HashMap<>();

        public TrackingSavedData(String name) {
            super(name);
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            StreamUtils.stream(nbt.getTagList("tracking_entries", Constants.NBT.TAG_COMPOUND))
                .forEachOrdered(b -> {
                    NBTTagCompound compound = (NBTTagCompound) b;
                    this.trackingMap.put(BlockPos.fromLong(compound.getLong("position")), compound.getString("name"));
                });
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound) {
            NBTTagList list = this.trackingMap.entrySet().stream()
                .map(e -> {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setLong("position", e.getKey().toLong());
                    nbt.setString("name", e.getValue());
                    return nbt;
                })
                .collect(IOCollectors.toNBTTagList());
            compound.setTag("tracking_entries", list);
            return compound;
        }

        public void set(BlockPos pos, String name) {
            this.trackingMap.put(pos, name);
            this.markDirty();
        }

        public void remove(BlockPos pos) {
            this.trackingMap.remove(pos);
            this.markDirty();
        }

        public List<TrackingSavedDataEntry> getList() {
            return Collections.unmodifiableList(this.trackingMap.entrySet().stream().map(e -> new TrackingSavedDataEntry(e.getKey(), e.getValue())).collect(Collectors.toList()));
        }
    }

    @Value
    public static class TrackingSavedDataEntry {
        private final BlockPos pos;
        private final String name;
    }
}
