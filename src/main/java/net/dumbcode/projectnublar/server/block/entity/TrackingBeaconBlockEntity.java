package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.server.ServerWorld;
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

    public TrackingBeaconBlockEntity() {
        super(ProjectNublarBlockEntities.TRACKING_BEACON.get());
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        this.name = compound.getString("name");
        this.radius = compound.getInt("radius");
        super.load(state, compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        compound.putString("name", this.name);
        compound.putInt("radius", this.radius);
        return super.save(compound);
    }

    @Override
    public void onLoad() {
        if(this.level instanceof ServerWorld) {
            getTrackingList((ServerWorld) this.level).set(this.worldPosition, this.name);
        }
        super.onLoad();
    }

    public void setName(String name) {
        this.name = name;
        this.setChanged();
        this.syncToClient();
        if(this.level instanceof ServerWorld) {
            getTrackingList((ServerWorld) this.level).set(this.worldPosition, this.name);
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
        this.setChanged();
        this.syncToClient();
    }

    public static TrackingSavedData getTrackingList(ServerWorld world) {
        return world.getDataStorage().computeIfAbsent(() -> new TrackingSavedData("tracking_beacon_data"), "tracking_beacon_data");
    }

    public static class TrackingSavedData extends WorldSavedData {

        private final Map<BlockPos, String> trackingMap = new HashMap<>();

        public TrackingSavedData(String name) {
            super(name);
        }

        @Override
        public void load(CompoundTag nbt) {
            StreamUtils.stream(nbt.getList("tracking_entries", Constants.NBT.TAG_COMPOUND))
                .forEachOrdered(b -> {
                    CompoundTag compound = (CompoundTag) b;
                    this.trackingMap.put(NBTUtil.readBlockPos(compound.getCompound("position")), compound.getString("name"));
                });
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            ListNBT list = this.trackingMap.entrySet().stream()
                .map(e -> {
                    CompoundTag nbt = new CompoundTag();
                    nbt.put("position", NBTUtil.writeBlockPos(e.getKey()));
                    nbt.putString("name", e.getValue());
                    return nbt;
                })
                .collect(CollectorUtils.toNBTTagList());
            compound.put("tracking_entries", list);
            return compound;
        }

        public void set(BlockPos pos, String name) {
            this.trackingMap.put(pos, name);
            this.setDirty();
        }

        public void remove(BlockPos pos) {
            this.trackingMap.remove(pos);
            this.setDirty();
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
