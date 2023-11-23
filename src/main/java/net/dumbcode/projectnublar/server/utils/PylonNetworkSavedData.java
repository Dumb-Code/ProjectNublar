package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.*;

@Getter
public class PylonNetworkSavedData extends WorldSavedData {

    public PylonNetworkSavedData(String name) {
        super(name);
    }

    private final Map<UUID, Set<BlockPos>> uuidPosMap = new HashMap<>();

    public Set<BlockPos> getPositions(UUID uuid) {
        return this.uuidPosMap.computeIfAbsent(uuid, u -> new HashSet<>());
    }

    public Set<BlockPos> getPositions(PylonHeadBlockEntity entity) {
        Set<BlockPos> set = this.getPositions(entity.getNetworkUUID());
        set.add(entity.getBlockPos());
        return set;
    }

    public void cleanAndRemove(PylonHeadBlockEntity entity) {
        if (this.uuidPosMap.containsKey(entity.getNetworkUUID())) {
            this.uuidPosMap.get(entity.getNetworkUUID()).remove(entity.getBlockPos());
        }
        this.clean();
    }

    public void cleanAndClear(UUID unusedNetwork) {
        this.uuidPosMap.remove(unusedNetwork);
        this.clean();
    }

    public void clean() {
        Set<UUID> toRemove = new HashSet<>();
        this.uuidPosMap.forEach((uuid, set) -> {
            if(set.isEmpty()) {
                toRemove.add(uuid);
            }
        });
        toRemove.forEach(this.uuidPosMap::remove);
    }

    @Override
    public void load(CompoundTag nbt) {
        this.uuidPosMap.clear();
        for (String s : nbt.getAllKeys()) {
            try {
                Set<BlockPos> set = new HashSet<>();
                for (INBT inbt : nbt.getList(s, Constants.NBT.TAG_COMPOUND)) {
                    set.add(NBTUtil.readBlockPos((CompoundTag) inbt));
                }
                this.uuidPosMap.put(UUID.fromString(s), set);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unable to parse networkUUID " + s + ". This is a fatal error, and the pylon network will not work correctly", e);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        this.uuidPosMap.forEach((uuid, set) -> {
            ListNBT list = new ListNBT();
            for (BlockPos pos : set) {
                list.add(NBTUtil.writeBlockPos(pos));
            }
            compound.put(uuid.toString(), list);
        });
        return compound;
    }

    public static PylonNetworkSavedData getData(ServerWorld world) {
        String identifier = "pylon_network";
        return world.getDataStorage().computeIfAbsent(() -> new PylonNetworkSavedData(identifier), identifier);
    }
}
