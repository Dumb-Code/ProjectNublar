package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.utils.PylonNetworkSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BlockPylonHead extends Block implements IItemBlock {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockPylonHead(Properties p_i48440_1_) {
        super(p_i48440_1_);
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(FACING, EnumFacing.UP));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack item = playerIn.getHeldItem(hand);
        if(item.getItem() == ItemHandler.WIRE_SPOOL && !worldIn.isRemote) {
            NBTTagCompound nbt = item.getOrCreateSubCompound(ProjectNublar.MODID);
            if(nbt.hasKey("pylon_position", Constants.NBT.TAG_LONG)) {
                BlockPos other = BlockPos.fromLong(nbt.getLong("pylon_position"));
                if(!pos.equals(other)) {
                    PylonHeadBlockEntity.Connection connection = new PylonHeadBlockEntity.Connection(other, pos);

                    TileEntity entity = worldIn.getTileEntity(pos);
                    TileEntity otherEntity = worldIn.getTileEntity(other);
                    if(entity instanceof PylonHeadBlockEntity && otherEntity instanceof PylonHeadBlockEntity) {
                        PylonHeadBlockEntity thisPylon = (PylonHeadBlockEntity) entity;
                        PylonHeadBlockEntity otherPylon = (PylonHeadBlockEntity) otherEntity;

                        UUID networkUUID = thisPylon.getNetworkUUID();
                        UUID oldNetworkUUID = otherPylon.getNetworkUUID();
                        if(!networkUUID.equals(oldNetworkUUID)) { //If the same an inter-network-connection is formed and we shouldn't mess with adding/removing
                            PylonNetworkSavedData data = PylonNetworkSavedData.getData(worldIn);

                            Set<BlockPos> newSet = data.getPositions(thisPylon);
                            Set<BlockPos> otherSet = data.getPositions(otherPylon);

                            for (BlockPos blockPos : otherSet) {
                                TileEntity te = worldIn.getTileEntity(blockPos);
                                if(te instanceof PylonHeadBlockEntity) {
                                    ((PylonHeadBlockEntity) te).setNetworkUUID(networkUUID);
                                    ((PylonHeadBlockEntity) te).syncToClient();
                                    newSet.add(blockPos);
                                    te.markDirty();
                                }
                            }
                            data.cleanAndClear(oldNetworkUUID);
                            data.markDirty();

                        }
                        thisPylon.addConnection(connection);
                        otherPylon.addConnection(connection);
                    }

                }
                nbt.removeTag("pylon_position");
            } else {
                nbt.setLong("pylon_position", pos.toLong());
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity old = worldIn.getTileEntity(pos);
        super.breakBlock(worldIn, pos, state);
        if(old instanceof PylonHeadBlockEntity) {
            //Remove connections from other pylons
            PylonHeadBlockEntity pylon = (PylonHeadBlockEntity) old;
            for (PylonHeadBlockEntity.Connection connection : pylon.getConnections()) {
                TileEntity otherEntity = worldIn.getTileEntity(connection.getOther(pos));
                if(otherEntity instanceof PylonHeadBlockEntity) {
                    ((PylonHeadBlockEntity) otherEntity).getConnections().remove(connection);
                    ((PylonHeadBlockEntity) otherEntity).syncToClient();
                    otherEntity.markDirty();
                }
            }

            //Remove this pylon from the pylon network
            PylonNetworkSavedData data = PylonNetworkSavedData.getData(worldIn);
            data.cleanAndClear(pylon.getNetworkUUID());


            //Make sure that pylons that are not split are have different pylon network IDs
            Set<Set<BlockPos>> branchedPositions = new HashSet<>();
            for (PylonHeadBlockEntity.Connection connection : pylon.getConnections()) {
                TileEntity otherEntity = worldIn.getTileEntity(connection.getOther(pos));
                if(otherEntity instanceof PylonHeadBlockEntity) {
                    branchedPositions.add(((PylonHeadBlockEntity) otherEntity).gatherAllNetworkLocations(new HashSet<>()));
                }
            }

            for (Set<BlockPos> set : branchedPositions) {
                UUID newUUID = UUID.randomUUID();
                Set<BlockPos> positions = data.getPositions(newUUID);
                for (BlockPos blockPos : set) {
                    TileEntity entity = worldIn.getTileEntity(blockPos);
                    if(entity instanceof PylonHeadBlockEntity) {
                        ((PylonHeadBlockEntity) entity).setNetworkUUID(newUUID);
                        entity.markDirty();
                        ((PylonHeadBlockEntity) entity).syncToClient();
                        positions.add(blockPos);
                    }
                }
            }
        }
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if(ProjectNublar.DEBUG) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof PylonHeadBlockEntity && !worldIn.isRemote) {
                Minecraft.getMinecraft().player.sendChatMessage(((PylonHeadBlockEntity) tileEntity).getNetworkUUID().toString());
            }
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        IBlockState s = worldIn.getBlockState(pos.down());
        if(s.getBlock() != BlockHandler.PYLON_POLE && !s.isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            worldIn.setBlockToAir(pos);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new PylonHeadBlockEntity();
    }
}
