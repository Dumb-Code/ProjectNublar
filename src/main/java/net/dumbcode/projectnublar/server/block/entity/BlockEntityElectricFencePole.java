package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class BlockEntityElectricFencePole extends SimpleBlockEntity implements ConnectableBlockEntity {
    public Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    public boolean rotatedAround = false;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            nbt.appendTag(connection.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("connections", nbt);
        compound.setBoolean("rotated", this.rotatedAround);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.rotatedAround = compound.getBoolean("rotated");
        this.fenceConnections.clear();
        NBTTagList nbt = compound.getTagList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbt.tagCount(); i++) {
            this.fenceConnections.add(Connection.fromNBT(nbt.getCompoundTagAt(i), this));
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Block block = this.world.getBlockState(this.pos).getBlock();
        return (block instanceof BlockElectricFencePole && this.world.getBlockState(this.pos).getValue(BlockElectricFencePole.INDEX_PROPERTY) == 0 ? new AxisAlignedBB(this.pos, this.pos.up(block instanceof BlockElectricFencePole ? ((BlockElectricFencePole)block).getType().getHeight() : 0)) : super.getRenderBoundingBox()).grow(1);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
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
