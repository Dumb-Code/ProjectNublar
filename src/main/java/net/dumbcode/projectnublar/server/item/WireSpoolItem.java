package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class WireSpoolItem extends BlockItem {

    public WireSpoolItem(Block superBlock, Item.Properties properties) {
        super(superBlock, properties);

    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
        Object hitInfo = context.hitResult.hitInfo;
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if(hitInfo instanceof BlockConnectableBase.HitChunk) {
            BlockConnectableBase.HitChunk chunk = (BlockConnectableBase.HitChunk) hitInfo;
            Direction dir = chunk.getDir();
            //Make sure that if its placed on the east/west side (the ends of the cables) to place the block on the previous/next positions
            if (dir == Direction.EAST) {
                pos = chunk.getConnection().getNext();
            } else if (dir == Direction.WEST) {
                pos = chunk.getConnection().getPrevious();
            }
        }
        boolean out =  super.placeBlock(BlockItemUseContext.at(context, pos, Direction.UP), state);
        if (world.getBlockState(pos).getBlock() == this.getBlock()) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                if (hitInfo instanceof BlockConnectableBase.HitChunk || hitInfo == null) {
                    BlockConnectableBase.generateConnections(world, pos, (ConnectableBlockEntity) te, (BlockConnectableBase.HitChunk) hitInfo, context.getClickedFace());
                }
                if (((ConnectableBlockEntity) te).getConnections().isEmpty()) {
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    return false;
                }
            }
        }

        return out;
    }


    public static void registerPropertyOverride(FMLClientSetupEvent event) {
        ItemModelsProperties.register(ItemHandler.WIRE_SPOOL.get(), new ResourceLocation(ProjectNublar.MODID, "distance"), (stack, worldIn, entityIn) -> {
            if (entityIn instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entityIn;
                ItemStack istack = entityIn.getMainHandItem();
                if (istack.getItem() == ItemHandler.WIRE_SPOOL.get()) {
                    CompoundNBT nbt = istack.getOrCreateTagElement(ProjectNublar.MODID);
                    BlockPos pos = null;
                    boolean isFence = false;
                    if (nbt.contains("fence_position", Constants.NBT.TAG_COMPOUND)) {
                        pos = NBTUtil.readBlockPos(nbt.getCompound("fence_position"));
                        isFence = true;
                    } else if (nbt.contains("pylon_position", Constants.NBT.TAG_COMPOUND)) {
                        pos = NBTUtil.readBlockPos(nbt.getCompound("pylon_position"));
                    }
                    if (pos != null) {
                        Block block = entityIn.level.getBlockState(pos).getBlock();
                        int multiplier = 1;
                        if (isFence && block instanceof BlockElectricFencePole) {
                            multiplier = ((BlockElectricFencePole) block).getType().getHeight();
                        }
                        double dist = player.position().distanceTo(new Vector3d(pos.getX(), pos.getY(), pos.getZ())) * multiplier;
                        for (ItemStack itemStack : player.inventory.items) {
                            if (itemStack.getItem() == ItemHandler.WIRE_SPOOL.get()) {
                                if (itemStack == stack) {
                                    if (dist <= BlockElectricFence.ITEM_FOLD * stack.getCount()) {
                                        return (float) dist / (BlockElectricFence.ITEM_FOLD * stack.getCount());
                                    } else {
                                        return 1F;
                                    }
                                }
                                dist -= BlockElectricFence.ITEM_FOLD * stack.getCount();
                            }
                        }
                    }
                }
            }

            return 0F;
        });
    }
}
