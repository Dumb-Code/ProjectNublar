package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.*;

@Getter
public class PylonNetworkSavedData extends WorldSavedData {

    public PylonNetworkSavedData(String name) {
        super(name);
    }

    private Map<UUID, Set<BlockPos>> uuidPosMap = new HashMap<>();

    public Set<BlockPos> getPositions(UUID uuid) {
        return this.uuidPosMap.computeIfAbsent(uuid, u -> new HashSet<>());
    }

    public Set<BlockPos> getPositions(PylonHeadBlockEntity entity) {
        Set<BlockPos> set = this.getPositions(entity.getNetworkUUID());
        set.add(entity.getPos());
        return set;
    }

    public void cleanAndRemove(PylonHeadBlockEntity entity) {
        if (this.uuidPosMap.containsKey(entity.getNetworkUUID())) {
            this.uuidPosMap.get(entity.getNetworkUUID()).remove(entity.getPos());
        }
        this.clean();
    }

    public void cleanAndClear(UUID unusedNetwork) {
        this.uuidPosMap.remove(unusedNetwork);
        this.clean();
    }

    public void clean() {
        this.uuidPosMap.values().removeIf(Set::isEmpty);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.uuidPosMap.clear();
        for (String s : nbt.getKeySet()) {
            try {
                Set<BlockPos> set = new HashSet<>();
                NBTBase tag = nbt.getTag(s);
                if(tag instanceof NBTTagLongArray) {
                    long[] along = ObfuscationReflectionHelper.getPrivateValue(NBTTagLongArray.class, (NBTTagLongArray)tag, "data", "field_193587_b");
                    for (long l : along) {
                        set.add(BlockPos.fromLong(l));
                    }
                }
                this.uuidPosMap.put(UUID.fromString(s), set);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unable to parse networkUUID " + s + ". This is a fatal error, and the pylon network will not work correctly", e);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        this.uuidPosMap.forEach((uuid, set) -> compound.setTag(uuid.toString(), new NBTTagLongArray(set.stream().mapToLong(BlockPos::toLong).toArray())));
        return compound;
    }

    public static PylonNetworkSavedData getData(World world) {
        String identifier = "pylon_network";
        PylonNetworkSavedData data = (PylonNetworkSavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(PylonNetworkSavedData.class, identifier);
        if(data == null) {
            data = new PylonNetworkSavedData(identifier);
            world.getMapStorage().setData(identifier, data);
        }
        return data;
    }
}
