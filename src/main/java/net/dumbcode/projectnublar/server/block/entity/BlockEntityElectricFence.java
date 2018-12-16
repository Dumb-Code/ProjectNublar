package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity implements ITickable {
    public Set<Connection> fenceConnections = Sets.newHashSet();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("from", connection.getFrom().toLong());
            tag.setLong("to", connection.getTo().toLong());

            nbt.appendTag(tag);
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
            NBTTagCompound tag = nbt.getCompoundTagAt(i);
            this.fenceConnections.add(new Connection(BlockPos.fromLong(tag.getLong("from")), BlockPos.fromLong(tag.getLong("to"))));
        }
    }

    public List<AxisAlignedBB> createBoundingBox() {
        List<AxisAlignedBB> out = Lists.newArrayList();
        for (Connection connections : this.fenceConnections) {
            BlockPos top = connections.getMax();
            BlockPos bottom = connections.getMin();

            double yrange = top.getY() == bottom.getY() ? 0 : (top.getY() - bottom.getY() - 1) / Math.sqrt((top.getX()-bottom.getX())*(top.getX()-bottom.getX()) + (top.getX()-bottom.getX())*(top.getX()-bottom.getX()));
            double[] intercects = LineUtils.liangBarskyIntersect(this.pos, connections.getFrom(), connections.getTo());
            if(intercects != null) {
                double amount = 16;

                double x = (intercects[1] - intercects[0]) / amount;
                double z = (intercects[3] - intercects[2]) / amount;

                for (int i = 0; i < amount; i++) {
                    int next = i + 1;
                    out.add(new AxisAlignedBB(x * i, 0F, z * i, x * next, 1F, z * next).offset(intercects[0] - this.pos.getX(), yrange * this.distance(bottom, intercects[0]+x*i, intercects[2]+z*i) - this.pos.getY() + bottom.getY(), intercects[2] - this.pos.getZ()));
                }
            }
        }
        return out;
    }
    
    private double distance(BlockPos pos, double x, double z) {
        return Math.sqrt((pos.getX()+0.5F-x)*(pos.getX()+0.5F-x) + (pos.getZ()+0.5F-z)*(pos.getZ()+0.5F-z));
    }

    @Override
    public void update() {
//        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());

    }

    @Getter
    public static class Connection {
        private final BlockPos from;
        private final BlockPos to;

        private final int compared;

        public Connection(BlockPos from, BlockPos to) {
            this.from = from;
            this.to = to;

            this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();
        }


        public BlockPos getMin() {
            return this.compared < 0 ? this.to : this.from;
        }

        public BlockPos getMax() {
            return this.compared >= 0 ? this.to : this.from;
        }
    }


}
