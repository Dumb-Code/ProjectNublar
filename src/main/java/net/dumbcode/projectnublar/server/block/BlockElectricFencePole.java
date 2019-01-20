package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class BlockElectricFencePole extends Block implements IItemBlock {
    @Getter
    private final ConnectionType type;
    public final PropertyInteger INDEX_PROPERTY;
    public static final PropertyRotation ROTATION_PROPERTY = PropertyRotation.create("rotation");

    private final BlockStateContainer blockState;

    public BlockElectricFencePole(ConnectionType type) {
        super(Material.IRON, MapColor.IRON);
        this.type = type;
        INDEX_PROPERTY = PropertyInteger.create("index", 0, type.getHeight());
        this.blockState = new BlockStateContainer(this, INDEX_PROPERTY, ROTATION_PROPERTY);

        this.setDefaultState(this.getBlockState().getBaseState().withProperty(INDEX_PROPERTY, 0));
    }

    @Override
    public BlockStateContainer getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        boolean flag = true;
        for (int i = 0; i < 3; i++) {
            flag &= worldIn.getBlockState(pos.up(i)).getBlock().isReplaceable(worldIn, pos.up(i));
        }
        return flag;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        float rotation = 0F; //Expensive calls ahead. Maybe try and cache them?
        TileEntity te = worldIn.getTileEntity(pos.down(state.getValue(INDEX_PROPERTY)));
        if(te instanceof BlockEntityElectricFencePole) {
            BlockEntityElectricFencePole ef = (BlockEntityElectricFencePole) te;
            if(!ef.fenceConnections.isEmpty()) {

                List<Connection> differingConnections = Lists.newArrayList();
                for (Connection connection : ef.fenceConnections) {
                    boolean has = false;
                    for (Connection dc : differingConnections) {
                        if(connection.getFrom().equals(dc.getFrom()) && connection.getTo().equals(dc.getTo())) {
                            has = true;
                            break;
                        }
                    }
                    if(!has) {
                        differingConnections.add(connection);
                    }
                }

                if (differingConnections.size() == 1) {
                    Connection connection = differingConnections.get(0);
                    double[] in = connection.getCache().getIn();
                    rotation = (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0]))) + 90F;
                } else {
                    Connection connection1 = differingConnections.get(0);
                    Connection connection2 = differingConnections.get(1);

                    double[] in1 = connection1.getCache().getIn();
                    double[] in2 = connection2.getCache().getIn();

                    double angle1 = MathUtils.horizontalDegree(in1[1] - in1[0], in1[2] - in1[3], connection1.getPosition().equals(connection1.getMin()));
                    double angle2 = MathUtils.horizontalDegree(in2[1] - in2[0], in2[2] - in2[3], connection2.getPosition().equals(connection2.getMin()));

                    rotation = (float) (angle1 + (angle2-angle1)/2D);
                }
            }

            rotation += this.getType().getRotationOffset() + 90F;
            if(ef.rotatedAround) {
                rotation += 180;
            }
        }
//        rotation = -rotation;
        while(rotation < 0) {
            rotation += 360;
        }
        return super.getActualState(state.withProperty(ROTATION_PROPERTY, MathHelper.floor(rotation % 360)), worldIn, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(INDEX_PROPERTY, 0);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if(state.getValue(INDEX_PROPERTY) == 0) {
            for (int i = 1; i < this.type.getHeight(); i++) {
                worldIn.setBlockState(pos.up(i), this.getDefaultState().withProperty(INDEX_PROPERTY, i));
            }

        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int index = state.getValue(INDEX_PROPERTY);
        if(index == 0) {
            ItemStack stack = playerIn.getHeldItem(hand);
            if(stack.isEmpty()) {
                TileEntity te = worldIn.getTileEntity(pos);
                if(te instanceof BlockEntityElectricFencePole) {
                    ((BlockEntityElectricFencePole) te).rotatedAround = !((BlockEntityElectricFencePole) te).rotatedAround;
                    te.markDirty();
                    for (int y = 0; y < this.type.getHeight(); y++) {
                        IBlockState blockState = worldIn.getBlockState(pos.up(y));
                        worldIn.notifyBlockUpdate(pos.up(y), blockState, blockState, 3);
                    }
                    return true;
                }
            } else if(stack.getItem() == Item.getItemFromBlock(BlockHandler.ELECTRIC_FENCE)) { //Move to item class ?
                NBTTagCompound nbt = stack.getOrCreateSubCompound(ProjectNublar.MODID);
                if(nbt.hasKey("fence_position", Constants.NBT.TAG_LONG)) {
                    BlockPos other = BlockPos.fromLong(nbt.getLong("fence_position"));
                    if(worldIn.getBlockState(other).getBlock() == this && !other.equals(pos)) {
                        for (double offset : this.type.getOffsets()) {
                            List<BlockPos> positions = LineUtils.getBlocksInbetween(pos, other, offset);
                            for (int i = 0; i < this.type.getHeight(); i++) {
                                BlockPos pos1 = pos.up(i);
                                BlockPos other1 = other.up(i);
                                for (int i1 = 0; i1 < positions.size(); i1++) {
                                    BlockPos position = positions.get(i1).up(i);
                                    if (worldIn.isAirBlock(position) || worldIn.getBlockState(position).getBlock().isReplaceable(worldIn, position)) {
                                        worldIn.setBlockState(position, BlockHandler.ELECTRIC_FENCE.getDefaultState());
                                    }
                                    TileEntity fencete = worldIn.getTileEntity(position);
                                    if (fencete instanceof ConnectableBlockEntity) {
                                        ((ConnectableBlockEntity) fencete).addConnection(new Connection(this.type, offset, pos1, other1, positions.get(Math.max(i1 - 1, 0)).up(i), positions.get(Math.min(i1 + 1, positions.size() - 1)).up(i), position));
                                        fencete.markDirty();
                                        worldIn.notifyBlockUpdate(position, worldIn.getBlockState(position), worldIn.getBlockState(position), 3);

                                    }
                                }
                            }
                        }

                    }
                    nbt.removeTag("fence_position");
                } else {
                    nbt.setLong("fence_position", pos.toLong());
                }
                return true;
            }
        } else if (worldIn.getBlockState(pos.down(index)).getBlock() == this) {
            return this.onBlockActivated(worldIn, pos.down(index), worldIn.getBlockState(pos.down(index)), playerIn, hand, facing, hitX, hitY, hitZ);
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    private static boolean destroying = false;

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if(!destroying) {
            destroying = true;
            int index = state.getValue(INDEX_PROPERTY);
            for (int i = 1; i < index+1; i++) {
                worldIn.setBlockToAir(pos.down(i)); //TODO: verify if our block?
            }
            for (int i = 1; i < this.type.getHeight()-index; i++) {
                worldIn.setBlockToAir(pos.up(i));
            }
            destroying = false;
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(INDEX_PROPERTY) == this.type.getHeight()-1 ? this.type.getLightLevel() : 0;
    }

    @Override
    public int getLightOpacity(IBlockState state) {
        return super.getLightOpacity(state);
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
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(INDEX_PROPERTY, meta);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(INDEX_PROPERTY);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntityElectricFencePole();
    }
}
