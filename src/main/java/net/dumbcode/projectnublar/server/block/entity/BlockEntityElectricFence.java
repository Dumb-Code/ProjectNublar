package net.dumbcode.projectnublar.server.block.entity;

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

    /**
     * Breaks the surrounding fence. Used for entities
     * who "attack" the fence.
     * @param intensity Intensity at which the fence breaks.
     */
    public void breakFence(int intensity) { // TODO: Add more randomness.
        for (Connection connection : fenceConnections) {
            for (double offset : connection.getType().getOffsets()) {
                List<BlockPos> blocks = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), offset);
                for (int k = 0; k < blocks.size(); k++) {
                    for (int i = 0; i < connection.getType().getHeight(); i++) {
                        BlockPos position = blocks.get(k).up(i);
                        if ((k == blocks.size() / 2 - 1 || k == blocks.size() / 2 + 1) && i < intensity / 2 + 1) {
                            world.destroyBlock(position, true);
                        } else if (k == blocks.size() / 2 && i < intensity) {
                            world.destroyBlock(position, true);
                        }
                    }
                }
            }
        }
    }
}
