package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity implements ConnectableBlockEntity {

    private Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            nbt.appendTag(connection.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("connections", nbt);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fenceConnections.clear();
        NBTTagList nbt = compound.getTagList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbt.tagCount(); i++) {
            Connection connection = Connection.fromNBT(nbt.getCompoundTagAt(i), this);
            if(connection.isValid()) {
                this.fenceConnections.add(connection);
            }
        }
    }

    @Override
    public boolean hasFastRenderer() {
        return false;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos.add(-1, -1, -1), this.pos.add(1, 1, 1));
    }

    @Override
    public void addConnection(Connection connection) {
        this.fenceConnections.add(connection);
    }

    @Override
    public Set<Connection> getConnections() {
        return this.fenceConnections;
    }

}
