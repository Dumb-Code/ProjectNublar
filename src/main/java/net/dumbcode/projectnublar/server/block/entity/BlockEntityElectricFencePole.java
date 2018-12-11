package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;

public class BlockEntityElectricFencePole extends SimpleBlockEntity {
    public Set<BlockPos> fenceConnections = Sets.newHashSet();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (BlockPos connection : this.fenceConnections) {
            nbt.appendTag(new NBTTagLong(connection.toLong()));
        }
        compound.setTag("connections", nbt);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fenceConnections.clear();
        NBTTagList nbt = compound.getTagList("connections", Constants.NBT.TAG_LONG);
        for (NBTBase base : nbt) {
            if(base.getId() == Constants.NBT.TAG_LONG && base instanceof NBTTagLong) {
                this.fenceConnections.add(BlockPos.fromLong(((NBTTagLong) base).getLong()));
            }
        }
    }
}
