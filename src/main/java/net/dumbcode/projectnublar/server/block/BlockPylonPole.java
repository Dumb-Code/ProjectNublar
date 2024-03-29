package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.block.entity.PylonHeadBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.ItemPylonPole;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

public class BlockPylonPole extends Block implements IItemBlock {

    private static final VoxelShape SHAPE = box(6, 0, 6, 10, 16, 10);

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public BlockPylonPole(Properties p_i48440_1_) {
        super(p_i48440_1_);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean drops) {
        BlockState s = worldIn.getBlockState(pos.below());
        if(s.getBlock() != BlockHandler.PYLON_POLE.get() && !Block.isFaceFull(s.getCollisionShape(worldIn, pos.below()), Direction.UP)) {
            worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, drops);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.getItem() == ItemHandler.WIRE_SPOOL.get()) {
            BlockPos testPos = pos;
            while(world.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE.get()) {
                testPos = testPos.above();
            }
            BlockState head = world.getBlockState(testPos);
            if(head.getBlock() == BlockHandler.PYLON_HEAD.get() && head.getValue(BlockPylonHead.FACING) == Direction.UP) {
                //Cannot use state#use as we need to pass the blockpos in manually.
                head.getBlock().use(head, world, testPos, player, hand, ray);
            }
        }
        return super.use(state, world, pos, player, hand, ray);
    }

    @Override
    public Item createItem(Item.Properties properties) {
        return new ItemPylonPole(properties);
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() == BlockHandler.PYLON_POLE.get()) {
            BlockPos pos = event.getPos();
            BlockPos testPos = event.getPos();
            World world = event.getPlayer().level;
            while(world.getBlockState(testPos).getBlock() == BlockHandler.PYLON_POLE.get()) {
                testPos = testPos.above();
            }
            BlockState head = world.getBlockState(testPos);
            if(head.getBlock() == BlockHandler.PYLON_HEAD.get() && head.getValue(BlockPylonHead.FACING) == Direction.UP) {
                TileEntity old = world.getBlockEntity(testPos);
                if(!world.isClientSide() && old instanceof PylonHeadBlockEntity) {
                    if(!event.getPlayer().isCreative()) {
                        InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(BlockHandler.PYLON_POLE.get()));
                    }
                    world.setBlock(testPos.below(), head, 3);
                    world.setBlock(testPos, Blocks.AIR.defaultBlockState(), 3);
                    ItemPylonPole.moveConnections((PylonHeadBlockEntity) old, testPos.below());
                }
                event.setCanceled(true);
            }
        }
    }

}
