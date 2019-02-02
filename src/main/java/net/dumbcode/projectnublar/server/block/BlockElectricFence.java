package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.Random;

public class BlockElectricFence extends BlockConnectableBase implements IItemBlock {

    public static final int ITEM_FOLD = 20;

    public BlockElectricFence() {
        super(Material.IRON, MapColor.IRON);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                if(connection.isBroken()) {
                    continue;
                 }
                Vector3d center = connection.getCache().getCenter();

                boolean pb = connection.brokenSide(world, connection.getPrevious());
                boolean nb = connection.brokenSide(world, connection.getNext());

                if(connection.getCompared() > 0) {
                    boolean ref = pb;
                    pb = nb;
                    nb = ref;
                }

                float chance = 0.02F;

                if(nb || pb) {
                    if(pb && rand.nextFloat() < chance) {
                        Vector3d point = new Vector3d();
                        point.sub(connection.getCache().getPrev().getPoint());
                        spawnParticles(world, point, center);
                    }

                    if(nb && rand.nextFloat() < chance) {
                        spawnParticles(world, connection.getCache().getNext().getPoint(), center);
                    }

                }
            }
        }
        super.randomDisplayTick(stateIn, world, pos, rand);
    }

    private void spawnParticles(World world,Vector3d point, Vector3d center) {
        Vector3d norm = new Vector3d();
        norm.normalize(point);
        ProjectNublar.spawnParticles(ParticleType.SPARKS, world, center.x+point.x, center.y+point.y, center.z+point.z, norm.x,norm.y,norm.z, 8);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BlockEntityElectricFence();
    }

    @Override
    public Item createItem() {
        return new FenceItemBlock();
    }

    public class FenceItemBlock extends ItemBlock {

        public FenceItemBlock() {
            super(BlockElectricFence.this);
            this.addPropertyOverride(new ResourceLocation(ProjectNublar.MODID, "distance"), (stack, worldIn, entityIn) -> {
                if(entityIn instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entityIn;
                    ItemStack istack = entityIn.getHeldItemMainhand();
                    if (istack.getItem() == Item.getItemFromBlock(BlockHandler.ELECTRIC_FENCE)) {
                        NBTTagCompound nbt = istack.getOrCreateSubCompound(ProjectNublar.MODID);
                        if (nbt.hasKey("fence_position", Constants.NBT.TAG_LONG)) {
                            BlockPos pos = BlockPos.fromLong(nbt.getLong("fence_position"));
                            Block block = entityIn.world.getBlockState(pos).getBlock();
                            int multiplier = 1;
                            if(block instanceof BlockElectricFencePole) {
                                multiplier = ((BlockElectricFencePole) block).getType().getHeight();
                            }
                            double dist = player.getPositionVector().distanceTo(new Vec3d(pos)) * multiplier;
                            for (ItemStack itemStack : player.inventory.mainInventory) {
                                if (itemStack.getItem() == Item.getItemFromBlock(BlockHandler.ELECTRIC_FENCE)) {
                                    if (itemStack == stack) {
                                        if (dist <= ITEM_FOLD * stack.getCount()) {
                                            return (float) dist / (ITEM_FOLD * stack.getCount());
                                        } else {
                                            return 1F;
                                        }
                                    }
                                    dist -= ITEM_FOLD * stack.getCount();
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
                    pos = chunk.getConnection().getPrevious();
                } else if(dir == EnumFacing.WEST) {
                    pos = chunk.getConnection().getNext();
                }
            }
            boolean out = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
            if(world.getBlockState(pos).getBlock() == this.block) {
                BlockElectricFence.this.placeBlockOnSide(world, result, pos, side);
            }
            return out;
        }
    }

}
