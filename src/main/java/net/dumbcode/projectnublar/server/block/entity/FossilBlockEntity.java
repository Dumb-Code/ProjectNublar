package net.dumbcode.projectnublar.server.block.entity;

import com.google.common.collect.Range;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.fossil.base.FossilTier;
import net.dumbcode.projectnublar.server.fossil.base.FossilType;
import net.dumbcode.projectnublar.server.fossil.base.StoneType;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FossilBlockEntity extends TileEntity {

    public static final ModelProperty<StoneType> STONE_TYPE = new ModelProperty<>();

    public static final ModelProperty<Fossil> FOSSIL = new ModelProperty<>();
    public static final ModelProperty<FossilTier> TIER = new ModelProperty<>();
    public static final ModelProperty<FossilType> TYPE = new ModelProperty<>();
    public static final ModelProperty<String> CRACKS_TEX = new ModelProperty<>();

    private StoneType stoneType;

    private Fossil fossil;
    private FossilTier tier;
    private FossilType type;

    public FossilBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public FossilBlockEntity() {
        this(ProjectNublarBlockEntities.FOSSIL.get());
        this.stoneType = StoneTypeHandler.GRANITE.get();
        this.fossil = FossilHandler.AMMONITE.get();
        this.tier = FossilTier.randomTier();
        this.type = FossilType.randomType();
    }

    @Override
    public void load(@Nonnull BlockState state, CompoundNBT nbt) {
        this.stoneType = StoneTypeHandler.STONE_TYPE_REGISTRY.get().getValue(new ResourceLocation(nbt.getString("stone_type")));
        this.fossil = FossilHandler.FOSSIL_REGISTRY.get().getValue(new ResourceLocation(nbt.getString("fossil")));
        this.tier = FossilTier.valueOf(nbt.getString("tier").toUpperCase());
        this.type = FossilType.valueOf(nbt.getString("type").toUpperCase());
        super.load(state, nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putString("stone_type", this.stoneType.getRegistryName().toString());
        nbt.putString("fossil", this.fossil.getRegistryName().toString());
        nbt.putString("tier", this.tier.name().toLowerCase());
        nbt.putString("type", this.type.name().toLowerCase());
        return super.save(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    public Fossil getFossil() {
        return fossil;
    }

    public void setFossil(Fossil fossil) {
        this.fossil = fossil;
        this.requestModelDataUpdate();
    }

    public StoneType getStoneType() {
        return stoneType;
    }

    public void setStoneType(StoneType stoneType) {
        this.stoneType = stoneType;
        this.requestModelDataUpdate();
    }

    public FossilTier getTier() {
        return tier;
    }

    public void setTier(FossilTier tier) {
        this.tier = tier;
        this.requestModelDataUpdate();
    }


    public FossilType getFossilType() {
        return type;
    }

    public void setType(FossilType type) {
        this.type = type;
        this.requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        if(this.remove) {
            return super.getModelData();
        }
        ModelDataMap.Builder builder = new ModelDataMap.Builder()
                .withInitial(FOSSIL, this.fossil)
                .withInitial(STONE_TYPE, this.stoneType)
                .withInitial(TIER, this.tier)
                .withInitial(TYPE, this.type);

//                .withInitial(CRACKS_TEX, ((FossilBlock)this.getBlockState().getBlock()).fossil.crackTexture.toString());
        return builder.build();
    }

    public List<ItemStack> drops() {
        int amountOfItemToDrop = chooseValueWithinRange(tier.getAmountOfFossilsToDrop());

        ItemStack stack = new ItemStack(ItemHandler.FOSSIL_ITEM.get(), amountOfItemToDrop);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("fossil", fossil.getRegistryName().toString());
        nbt.putFloat("tierDNAMultiplier", tier.getDNAGatherChance());
        nbt.putFloat("typeDNAMultiplier", type.getDNAMultiplier());
//TODO:        nbt.putFloat("stoneTypeDNAMultiplier", stoneType.getDNAMultiplier());
//TODO:        nbt.putFloat("fossilDNAMultiplier", fossil.getDNAMultiplier());
        stack.addTagElement(ProjectNublar.MODID, nbt);

        return Collections.singletonList(stack);
    }

    private int chooseValueWithinRange(Range<Integer> amountOfFossilsToDrop) {
        int lowerBound = amountOfFossilsToDrop.lowerEndpoint();
        int upperBound = amountOfFossilsToDrop.upperEndpoint();

        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound + 1);
    }
}
