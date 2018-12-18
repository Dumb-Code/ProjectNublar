package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity {
    public Set<Connection> fenceConnections = Sets.newHashSet();

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
        List<AxisAlignedBB> out = Lists.newArrayList();
        for (Connection connections : this.fenceConnections) {
            BlockPos top = connections.getMax();
            BlockPos bottom = connections.getMin();

            double yrange = (top.getY() - bottom.getY()) / Math.sqrt((top.getX()-bottom.getX())*(top.getX()-bottom.getX()) + (top.getZ()-bottom.getZ())*(top.getZ()-bottom.getZ()));
            double[] intersect = LineUtils.intersect(this.pos, connections.getFrom(), connections.getTo(), 0.5F);
            if(intersect != null) {
                double amount = 16;

                double x = (intersect[1] - intersect[0]) / amount;
                double z = (intersect[3] - intersect[2]) / amount;

                for (int i = 0; i < amount; i++) {
                    int next = i + 1;
                    out.add(new AxisAlignedBB(x * i, 0F, z * i, x * next, 1F, z * next).offset(intersect[0] - this.pos.getX(), yrange * this.distance(bottom, intersect[0]+x*i, intersect[2]+z*i) - this.pos.getY() + bottom.getY(), intersect[2] - this.pos.getZ()));
                }
            }
        }
        return out;
    }
    
    private double distance(BlockPos pos, double x, double z) {
        return Math.sqrt((pos.getX()+0.5F-x)*(pos.getX()+0.5F-x) + (pos.getZ()+0.5F-z)*(pos.getZ()+0.5F-z));
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

}
