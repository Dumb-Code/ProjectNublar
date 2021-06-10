package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Sets;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.projectnublar.client.render.model.ProjectNublarModelData;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Signed;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockEntityElectricFence extends SimpleBlockEntity implements ConnectableBlockEntity {

    private final Set<Connection> fenceConnections = Sets.newLinkedHashSet();

    private VoxelShape collidableCache;

    public BlockEntityElectricFence(TileEntityType<?> type) {
        super(type);
    }

    public BlockEntityElectricFence() {
        super(ProjectNublarBlockEntities.ELECTRIC_FENCE.get());
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT nbt = new ListNBT();
        for (Connection connection : this.fenceConnections) {
            nbt.add(connection.writeToNBT(new CompoundNBT()));
        }
        compound.put("connections", nbt);
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.fenceConnections.clear();
        ListNBT nbt = compound.getList("connections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbt.size(); i++) {
            Connection connection = Connection.fromNBT(nbt.getCompound(i), this);
            if(connection.isValid()) {
                this.fenceConnections.add(connection);
            }
        }

        if(this.level != null) {
            this.requestModelDataUpdate();
        }
    }

    @Override
    public VoxelShape getOrCreateCollision() {
        if(this.collidableCache == null) {
            VoxelShape shape = VoxelShapes.empty();
            for (BlockConnectableBase.ConnectionAxisAlignedBB bb : BlockConnectableBase.createBoundingBox(this.getConnections(), this.getBlockPos())) {
                shape = VoxelShapes.or(shape, VoxelShapes.create(bb));
            }
            this.collidableCache = shape;
        }
        return this.collidableCache;
    }

    @Override
    public double getViewDistance() {
        return Double.MAX_VALUE;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.getBlockPos().offset(-1, -1, -1), this.getBlockPos().offset(1, 1, 1));
    }

    @Override
    public void addConnection(Connection connection) {
        this.fenceConnections.add(connection);
        this.requestModelDataUpdate();
        this.setChanged();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
            .withInitial(ProjectNublarModelData.CONNECTIONS, this.compiledRenderData())
            .build();
    }

    @Override
    public void requestModelDataUpdate() {
        super.requestModelDataUpdate();
        if(this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        this.collidableCache = null;
    }

    @OnlyIn(Dist.CLIENT)
    protected Set<Connection.CompiledRenderData> compiledRenderData() {
        return this.getConnections().stream()
            .map(c -> c.compileRenderData(this.level))
            .collect(Collectors.toSet());
    }

    @Override
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(this.fenceConnections);
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
                        BlockPos position = blocks.get(k).above(i);
                        if ((k == blocks.size() / 2 - 1 || k == blocks.size() / 2 + 1) && i < intensity / 2 + 1) {
                            this.level.destroyBlock(position, true);
                        } else if (k == blocks.size() / 2 && i < intensity) {
                            this.level.destroyBlock(position, true);
                        }
                    }
                }
            }
        }
    }
}
