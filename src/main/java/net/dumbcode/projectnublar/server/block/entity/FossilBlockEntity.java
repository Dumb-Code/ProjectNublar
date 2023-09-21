package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.fossil.base.FossilTier;
import net.dumbcode.projectnublar.server.fossil.base.FossilType;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nonnull;

//TODO: make this get block data and pass it into a baked model
public class FossilBlockEntity extends TileEntity {
    private FossilTier tier;
    private FossilType type;

    public FossilBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public FossilBlockEntity() {
        this(ProjectNublarBlockEntities.FOSSIL.get());
        this.tier = FossilTier.randomTier();
        this.type = FossilType.randomType();
    }

    @Override
    public void load(@Nonnull BlockState state, CompoundNBT nbt) {
        tier = FossilTier.valueOf(nbt.getString("tier").toUpperCase());
        type = FossilType.valueOf(nbt.getString("type").toUpperCase());
        super.load(state, nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        if (tier == null) {
            tier = FossilTier.randomTier();
        }
        if (type == null) {
            type = FossilType.randomType();
        }
        nbt.putString("tier", tier.name().toLowerCase());
        nbt.putString("type", type.name().toLowerCase());
        return super.save(nbt);
    }
}