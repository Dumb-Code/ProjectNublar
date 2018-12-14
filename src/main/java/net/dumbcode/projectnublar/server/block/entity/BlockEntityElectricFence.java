package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity implements ITickable {
    public Set<Pair<BlockPos, BlockPos>> fenceConnections = Sets.newHashSet();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Pair<BlockPos, BlockPos> connection : this.fenceConnections) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("from", connection.getKey().toLong());
            tag.setLong("to", connection.getValue().toLong());

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
            this.fenceConnections.add(Pair.of(BlockPos.fromLong(tag.getLong("from")), BlockPos.fromLong(tag.getLong("to"))));
        }
    }

    public List<AxisAlignedBB> createBoundingBox() {
        List<AxisAlignedBB> out = Lists.newArrayList();
        for (Pair<BlockPos, BlockPos> connections : this.fenceConnections) {
            Vector3d from = new Vector3d(connections.getLeft().getX() + 0.5, connections.getLeft().getY() + 0.5, connections.getLeft().getZ() + 0.5);
            Vector3d to = new Vector3d(connections.getRight().getX() + 0.5, connections.getRight().getY() + 0.5, connections.getRight().getZ() + 0.5);

            Vector3d top = from.getY() > to.getY() ? from : to;
            Vector3d bottom = from.getY() <= to.getY() ? from : to;

            double yrange = (top.getY() - bottom.getY()) / Math.sqrt((top.x-bottom.x)*(top.x-bottom.x) + (top.y-bottom.y)*(top.y-bottom.y) + (top.z-bottom.z)*(top.z-bottom.z));
            double[] intercects = LineUtils.liangBarskyIntersect(this.pos.getX(), this.pos.getX() + 1, this.pos.getZ(), this.pos.getZ() + 1, from.getX(), to.getX(), from.getZ(), to.getZ());
            if(intercects != null) {
                double amount = 16;

                double x = (intercects[1] - intercects[0]) / amount;
                double z = (intercects[3] - intercects[2]) / amount;

                for (int i = 0; i < amount; i++) {
                    int next = i + 1;
                    out.add(new AxisAlignedBB(x * i, -0.5F, z * i, x * next, 0.5F, z * next).offset(intercects[0] - this.pos.getX(), yrange * this.distance(bottom, intercects[0]+x*i, bottom.getY(), intercects[2]+z*i) - this.pos.getY() + bottom.getY(), intercects[2] - this.pos.getZ()));
                }

            }

        }
        return out;
    }
    
    private double distance(Vector3d vec, double x, double y, double z) {
        return Math.sqrt((vec.x-x)*(vec.x-x) + (vec.y-y)*(vec.y-y) + (vec.z-z)*(vec.z-z));
    }

    @Override
    public void update() {
//        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());

    }
}
