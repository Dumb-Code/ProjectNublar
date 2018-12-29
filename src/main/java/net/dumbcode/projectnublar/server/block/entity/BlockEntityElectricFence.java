package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity implements ConnectableBlockEntity {
    public Set<Connection> fenceConnections = Sets.newLinkedHashSet();

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
            this.fenceConnections.add(Connection.fromNBT(nbt.getCompoundTagAt(i), this));
        }
    }

    public List<AxisAlignedBB> createBoundingBox() {
        Block block = this.world.getBlockState(this.pos).getBlock();
        List<AxisAlignedBB> out = Lists.newArrayList();
        if(block instanceof BlockElectricFence) {
            for (Connection connections : this.fenceConnections) {
                for (int off = 0; off < connections.getType().getOffsets().length; off++) {
                    double[] intersect = LineUtils.intersect(this.pos, connections.getFrom(), connections.getTo(), connections.getType().getOffsets()[off]);
                    if(intersect != null) {
                        double amount = 16;

                        double x = (intersect[1] - intersect[0]) / amount;
                        double y = (intersect[5] - intersect[4]) / amount;
                        double z = (intersect[3] - intersect[2]) / amount;

                        for (int i = 0; i < amount; i++) {
                            int next = i + 1;
                            out.add(new AxisAlignedBB(x * i, y * i, z * i, x * next, y * next, z * next).offset(intersect[0] - this.pos.getX(), intersect[4] - this.pos.getY(), intersect[2] - this.pos.getZ()).grow(0, connections.getCache(off).getFullThick()/2D, 0));
                        }
                    }
                }
            }
        }
        return out;
    }

    @Override
    public boolean hasFastRenderer() {
        return false;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 2048*2048;
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
