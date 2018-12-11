package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public class BlockEntityElectricFence extends SimpleBlockEntity {
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
            double[] intercects = LineUtils.liangBarskyIntersect(this.pos.getX(), this.pos.getX() + 1, this.pos.getZ(), this.pos.getZ() + 1, connections.getKey().getX() + 0.5, connections.getValue().getX() + 0.5, connections.getKey().getZ() + 0.5, connections.getValue().getZ() + 0.5);
            if(intercects != null) {
                double amount = 16;

                double x = (intercects[1] - intercects[0]) / amount;
                double z = (intercects[3] - intercects[2]) / amount;

                for (int i = 0; i < amount; i++) {
                    int next = i + 1;
                    out.add(new AxisAlignedBB(x * i, 0, z * i, x * next, 1, z * next).offset(intercects[0] - this.pos.getX(), 0, intercects[2] - this.pos.getZ()));
                }

            }

        }
        return out;
    }
}
