package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockConnectableBase;
import net.dumbcode.projectnublar.server.block.BlockElectricFence;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;

public class WireSpoolItem extends ItemBlock {

    public WireSpoolItem(Block superBlock) {
        super(superBlock);
            this.addPropertyOverride(new ResourceLocation(ProjectNublar.MODID, "distance"), (stack, worldIn, entityIn) -> {
                if(entityIn instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityIn;
                    ItemStack istack = entityIn.getHeldItemMainhand();
                    if (istack.getItem() == ItemHandler.WIRE_SPOOL) {
                        NBTTagCompound nbt = istack.getOrCreateSubCompound(ProjectNublar.MODID);
                        BlockPos pos = null;
                        boolean isFence = false;
                        if (nbt.hasKey("fence_position", Constants.NBT.TAG_LONG)) {
                            pos = BlockPos.fromLong(nbt.getLong("fence_position"));
                            isFence = true;
                        } else if (nbt.hasKey("pylon_position", Constants.NBT.TAG_LONG)) {
                            pos = BlockPos.fromLong(nbt.getLong("pylon_position"));
                        }
                        if (pos != null) {
                            Block block = entityIn.world.getBlockState(pos).getBlock();
                            int multiplier = 1;
                            if(isFence && block instanceof BlockElectricFencePole) {
                                multiplier = ((BlockElectricFencePole) block).getType().getHeight();
                            }
                            double dist = player.getPositionVector().distanceTo(new Vec3d(pos)) * multiplier;
                            for (ItemStack itemStack : player.inventory.mainInventory) {
                                if (itemStack.getItem() == ItemHandler.WIRE_SPOOL) {
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

        @Override
        public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
            RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
            if(result != null && result.hitInfo instanceof BlockConnectableBase.HitChunk) {
                BlockConnectableBase.HitChunk chunk = (BlockConnectableBase.HitChunk) result.hitInfo;
                EnumFacing dir = chunk.getDir();
                //Make sure that if its placed on the east/west side (the ends of the cables) to place the block on the previous/next positions
                if(dir == EnumFacing.EAST) {
                    pos = chunk.getConnection().getNext();
                } else if(dir == EnumFacing.WEST) {
                    pos = chunk.getConnection().getPrevious();
                }
            }
            boolean out = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
            if(world.getBlockState(pos).getBlock() == this.block) {
                TileEntity te = world.getTileEntity(pos);
                if(te instanceof ConnectableBlockEntity) {
                    if (result != null && (result.hitInfo instanceof BlockConnectableBase.HitChunk || result.hitInfo == null)) {
                        BlockConnectableBase.generateConnections(world, pos, (ConnectableBlockEntity) te, (BlockConnectableBase.HitChunk) result.hitInfo, side);
                    }
                    if(((ConnectableBlockEntity) te).getConnections().isEmpty()) {
                        world.setBlockToAir(pos);
                        return false;
                    }
                }
            }
            return out;
        }
    }
