package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.ProjectNublarBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockCreativePowerSource extends Block implements IItemBlock {


    public BlockCreativePowerSource(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockEntity();
    }

    public static class BlockEntity extends TileEntity implements ITickableTileEntity {
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

        private final LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> this.storage);

        public BlockEntity() {
            super(ProjectNublarBlockEntities.CREATIVE_ENERGY.get());
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.capability);
        }

        @Override
        public void tick() {
            for (Direction value : Direction.values()) {
                TileEntity te = this.level.getBlockEntity(this.worldPosition.relative(value));
                if(te != null) {
                    te.getCapability(CapabilityEnergy.ENERGY).ifPresent(c -> c.receiveEnergy(c.getMaxEnergyStored(), false));
                }
            }
        }
    }
}
