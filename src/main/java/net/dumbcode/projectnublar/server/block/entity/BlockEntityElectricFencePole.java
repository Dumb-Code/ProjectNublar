package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BlockEntityElectricFencePole extends SimpleBlockEntity implements ConnectableBlockEntity, ITickable {
    public Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    public boolean rotatedAround = false;

    private MachineModuleEnergyStorage energy = new MachineModuleEnergyStorage(3000, 700, 400);

    public boolean powered;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList nbt = new NBTTagList();
        for (Connection connection : this.fenceConnections) {
            nbt.appendTag(connection.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("connections", nbt);
        compound.setBoolean("rotated", this.rotatedAround);

        compound.setBoolean("powered", this.powered);

        NBTTagCompound energyNBT = new NBTTagCompound();
        energyNBT.setInteger("Amount", this.energy.getEnergyStored());
        compound.setTag("Energy", energyNBT);

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.rotatedAround = compound.getBoolean("rotated");
        this.powered = compound.getBoolean("powered");
        this.fenceConnections.clear();
        NBTTagList nbt = compound.getTagList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbt.tagCount(); i++) {
            this.fenceConnections.add(Connection.fromNBT(nbt.getCompoundTagAt(i), this));
        }


        NBTTagCompound energyNBT = compound.getCompoundTag("Energy");
        this.energy = new MachineModuleEnergyStorage(3000, 700, 250, energyNBT.getInteger("Amount"));
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
        boolean powered = this.energy.getEnergyStored() > 0;
        boolean update = false;
        if(this.powered != powered) {
            update = true;
        }
        this.powered = powered;


        this.energy.extractRaw(10);
        IBlockState state = this.world.getBlockState(this.pos);
        if(state.getBlock() instanceof BlockElectricFencePole && state.getValue(((BlockElectricFencePole) state.getBlock()).INDEX_PROPERTY) == 0) {
            if(update) {
                this.world.markBlockRangeForRenderUpdate(this.pos, this.pos.up(((BlockElectricFencePole) state.getBlock()).getType().getHeight()));
            }
            if(this.energy.getEnergyStored() > 600) {
                Set<IEnergyStorage> storages = Sets.newLinkedHashSet();
                for (Connection connection : this.fenceConnections) {
                    TileEntity te = this.world.getTileEntity(connection.getPosition().equals(connection.getFrom()) ? connection.getTo() : connection.getFrom());
                    if(te != null && te.hasCapability(CapabilityEnergy.ENERGY, null)) {
                        storages.add(Objects.requireNonNull(te.getCapability(CapabilityEnergy.ENERGY, null)));
                    }
                }
                List<IEnergyStorage> list = Lists.newArrayList(storages);
                list.sort(Comparator.comparing(IEnergyStorage::getEnergyStored));
                for (IEnergyStorage storage : list) {
                    int sendEnergy = storage.receiveEnergy(this.energy.extractEnergy(300 / list.size(), true), true);
                    this.energy.extractEnergy(sendEnergy, false);
                    storage.receiveEnergy(sendEnergy, false);
                }
            }
        }
    }
}
