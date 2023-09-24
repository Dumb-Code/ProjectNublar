package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.fossil.Fossils;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.FossilTier;
import net.dumbcode.projectnublar.server.fossil.base.FossilType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;

//TODO: make this get block data and pass it into a baked model
public class FossilBlockEntity extends TileEntity {

    public static final ModelProperty<BlockState> BASE_STATE = new ModelProperty<>();

    public static final ModelProperty<Fossil> FOSSILS = new ModelProperty<>();
    public static final ModelProperty<FossilTier> TIER = new ModelProperty<>();
    public static final ModelProperty<FossilType> TYPE = new ModelProperty<>();
    public static final ModelProperty<String> CRACKS_TEX = new ModelProperty<>();

    private BlockState baseState;

    private Fossil fossil;
    private FossilTier tier;
    private FossilType type;

    public FossilBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public FossilBlockEntity() {
        this(ProjectNublarBlockEntities.FOSSIL.get());
        this.baseState = Blocks.STONE.defaultBlockState();
        this.fossil = Fossils.FOSSILS.get(0);
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
    public IModelData getModelData() {
        if(this.remove) {
            return super.getModelData();
        }
        ModelDataMap.Builder builder = new ModelDataMap.Builder()
                .withInitial(TIER, this.tier)
                .withInitial(TYPE, this.type);
//                .withInitial(CRACKS_TEX, ((FossilBlock)this.getBlockState().getBlock()).fossil.crackTexture.toString());
        return builder.build();
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