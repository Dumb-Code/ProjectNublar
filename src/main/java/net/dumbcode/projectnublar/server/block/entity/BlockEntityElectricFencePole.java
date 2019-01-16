package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.ConnectionType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Set;

public class BlockEntityElectricFencePole extends SimpleBlockEntity implements ConnectableBlockEntity, ITickable {
    public Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    public boolean rotatedAround = false;

    private MachineModuleEnergyStorage energy = new MachineModuleEnergyStorage(30000, 200, 0);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            nbt.appendTag(connection.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("connections", nbt);
        compound.setBoolean("rotated", this.rotatedAround);

        NBTTagCompound energyNBT = new NBTTagCompound();
        energyNBT.setInteger("Amount", this.energy.getEnergyStored());
        compound.setTag("Energy", energyNBT);

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


        NBTTagCompound energyNBT = compound.getCompoundTag("Energy");
        this.energy = new MachineModuleEnergyStorage(30000, 200, 0, energyNBT.getInteger("Amount"));
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

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            IBlockState state = this.world.getBlockState(this.pos);
            if(state.getBlock() instanceof BlockElectricFencePole) {
                TileEntity base = this.world.getTileEntity(this.pos.down(state.getValue(((BlockElectricFencePole) state.getBlock()).INDEX_PROPERTY)));
                if(base instanceof BlockEntityElectricFencePole) {
                    return true;
                }
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            IBlockState state = this.world.getBlockState(this.pos);
            if(state.getBlock() instanceof BlockElectricFencePole) {
                TileEntity base = this.world.getTileEntity(this.pos.down(state.getValue(((BlockElectricFencePole) state.getBlock()).INDEX_PROPERTY)));
                if(base instanceof BlockEntityElectricFencePole) {
                    return CapabilityEnergy.ENERGY.cast(((BlockEntityElectricFencePole) base).energy);
                }
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void update() {
        this.energy.extractRaw(50); // TODO: config
    }
}
