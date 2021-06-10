package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.render.model.ProjectNublarModelData;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.ConnectionType;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BlockEntityElectricFencePole extends BlockEntityElectricFence implements ConnectableBlockEntity, ITickableTileEntity {

    @Getter
    private boolean flippedAround;

    @Getter
    private VoxelShape cachedShape = VoxelShapes.block();

    private double cachedRotation = 0;

    private final MachineModuleEnergyStorage energy = new MachineModuleEnergyStorage(350, 350, 250);
    private final LazyOptional<MachineModuleEnergyStorage> energyCap = LazyOptional.of(() -> this.energy);

    public BlockEntityElectricFencePole() {
        super(ProjectNublarBlockEntities.ELECTRIC_FENCE_POLE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.putBoolean("rotation_flipped", this.flippedAround);
        compound.putInt("energy", this.energy.getEnergyStored());

        return super.save(compound);
    }


    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.flippedAround = compound.getBoolean("rotation_flipped");
        this.energy.setEnergy(compound.getInt("energy"));
    }

    @Override
    public boolean removedByFenceRemovers() {
        return false;
    }


    public void setFlippedAround(boolean flippedAround) {
        this.flippedAround = flippedAround;
        this.requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            BlockState state = this.level.getBlockState(this.getBlockPos());
            if (state.getBlock() instanceof BlockElectricFencePole) {
                TileEntity base = this.level.getBlockEntity(this.getBlockPos().below(state.getValue(((BlockElectricFencePole) state.getBlock()).indexProperty)));
                if (base instanceof BlockEntityElectricFencePole) {
                    return ((BlockEntityElectricFencePole) base).energyCap.cast();
                }
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tick() {
        boolean powered = this.energy.getEnergyStored() > 0;
        boolean update = false;
        if (this.level.getBlockState(this.getBlockPos()).getValue(BlockElectricFencePole.POWERED_PROPERTY) != powered) {
            update = true;
        }

        this.energy.extractRaw(10);
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof BlockElectricFencePole && state.getValue(((BlockElectricFencePole) state.getBlock()).indexProperty) == 0) {
            if (update) {
                for (int y = 0; y < ((BlockElectricFencePole) state.getBlock()).getType().getHeight(); y++) {
                    BlockPos pos = this.getBlockPos().above(y);
                    BlockState s = this.level.getBlockState(pos);
                    if (s.getBlock() == state.getBlock()) { //When placing the blocks can be air
                        this.level.setBlock(pos, s.setValue(BlockElectricFencePole.POWERED_PROPERTY, powered), 3);
                    }
                }
            }
            //Pass power to other poles connected to this.
            if (this.energy.getEnergyStored() > 300) {
                Set<IEnergyStorage> storages = Sets.newLinkedHashSet();
                for (Connection connection : this.getConnections()) {
                    TileEntity te = this.level.getBlockEntity(connection.getPosition().equals(connection.getFrom()) ? connection.getTo() : connection.getFrom());
                    if (te != null) {
                        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(storages::add);
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



    @Override
    public void requestModelDataUpdate() {
        this.cachedRotation = this.computeRotation();

        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof BlockElectricFencePole) {
            ConnectionType type = ((BlockElectricFencePole) state.getBlock()).getType();

            float t = type.getHalfSize();
            double x = Math.sin(this.cachedRotation) * type.getRadius();
            double z = Math.cos(this.cachedRotation) * type.getRadius();
            this.cachedShape = VoxelShapes.box(x-t, 0, z-t, x+t, 1, z+t).move(0.5, 0, 0.5);
        }

        super.requestModelDataUpdate();

    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
            .withInitial(ProjectNublarModelData.CONNECTIONS, this.compiledRenderData())
            .withInitial(ProjectNublarModelData.FENCE_POLE_ROTATION_DEGS, this.cachedRotation)
            .build();
    }

    private double computeRotation() {
        double rotation = this.flippedAround ? 0 : 180F;
        if(this.level == null) {
            return rotation;
        }
        BlockState state = this.level.getBlockState(this.getBlockPos());
        if (state.getBlock() instanceof BlockElectricFencePole) {
            BlockElectricFencePole pole = (BlockElectricFencePole) state.getBlock();
            TileEntity te = this.level.getBlockEntity(this.getBlockPos().below(state.getValue((pole).indexProperty)));
            if (te instanceof BlockEntityElectricFencePole) {
                BlockEntityElectricFencePole ef = (BlockEntityElectricFencePole) te;
                if (!ef.getConnections().isEmpty()) {

                    List<Connection> differingConnections = Lists.newArrayList();
                    for (Connection connection : ef.getConnections()) {
                        boolean has = false;
                        for (Connection dc : differingConnections) {
                            if (connection.getFrom().equals(dc.getFrom()) && connection.getTo().equals(dc.getTo())) {
                                has = true;
                                break;
                            }
                        }
                        if (!has) {
                            differingConnections.add(connection);
                        }
                    }

                    if (differingConnections.size() == 1) {
                        Connection connection = differingConnections.get(0);
                        double[] in = connection.getIn();
                        rotation += (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0])));
                    } else {
                        Connection connection1 = differingConnections.get(0);
                        Connection connection2 = differingConnections.get(1);

                        double[] in1 = connection1.getIn();
                        double[] in2 = connection2.getIn();

                        double angle1 = MathUtils.horizontalDegree(in1[1] - in1[0], in1[2] - in1[3], connection1.getPosition().equals(connection1.getMin()));
                        double angle2 = MathUtils.horizontalDegree(in2[1] - in2[0], in2[2] - in2[3], connection2.getPosition().equals(connection2.getMin()));

                        rotation += (float) (angle1 + (angle2 - angle1) / 2D);
                    }
                }

                rotation += pole.getType().getRotationOffset() + 90F;
                if (ef.isFlippedAround()) {
                    rotation += 180;
                }
            }
            while (rotation < 0) {
                rotation += 360;
            }
        }
        return rotation;
    }
}
