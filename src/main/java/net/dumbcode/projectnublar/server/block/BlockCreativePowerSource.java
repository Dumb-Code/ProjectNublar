package net.dumbcode.projectnublar.server.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class BlockCreativePowerSource extends Block implements IItemBlock {


    public BlockCreativePowerSource(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityCreativePowerSource();
    }

    public static class TileEntityCreativePowerSource extends TileEntity implements ITickable {
        private IEnergyStorage storage = new EnergyStorage(0) {

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return maxExtract;
            }

            @Override
            public boolean canReceive() {
                return false;
            }
        };

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            if(capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.cast(this.storage);
            }
            return super.getCapability(capability, facing);
        }

        @Override
        public void update() {
            for (EnumFacing value : EnumFacing.values()) {
                TileEntity te = this.world.getTileEntity(this.pos.offset(value));
                if(te != null && te.hasCapability(CapabilityEnergy.ENERGY, value.getOpposite())) {
                    IEnergyStorage capability = te.getCapability(CapabilityEnergy.ENERGY, value.getOpposite());
                    if(capability != null) {
                        capability.receiveEnergy(capability.getMaxEnergyStored(), false);
                    }
                }
            }
        }
    }
}
