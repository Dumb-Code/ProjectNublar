package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Sets;
import lombok.Value;
import net.dumbcode.projectnublar.client.utils.DebugUtil;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class BlockElectricFence extends Block implements IItemBlock {

    public static final boolean DEBUG = false;

    public BlockElectricFence() {
        super(Material.IRON, MapColor.IRON);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if(event.getFace() != null) {
            TileEntity tile = world.getTileEntity(event.getPos().offset(event.getFace()));

        }

    }

    @SubscribeEvent
    public static void onDrawBlock(DrawBlockHighlightEvent event) {
        if(event.getSubID() == 0) {
            RayTraceResult target = event.getTarget();
            if(target.typeOfHit == RayTraceResult.Type.BLOCK && target.hitInfo instanceof Chunk) {
                World world = Minecraft.getMinecraft().world;
                BlockPos pos = target.getBlockPos();
                EntityPlayer player = Minecraft.getMinecraft().player;
                IBlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof BlockElectricFence) {
                    Chunk chunk = (Chunk) target.hitInfo;
                    event.setCanceled(true);
                    double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
                    double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
                    double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(2.0F);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);

                    Connection.Cache cache = chunk.connection.getCache(chunk.connectionID);
                    double[] in = cache.getIn();


                    AxisAlignedBB aabb = chunk.aabb;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-d3, -d4, -d5);
                    GlStateManager.translate(in[0], in[4], in[2]);

                    if(DEBUG) {
                        DebugUtil.renderFenceCollision(event);
                    }

                    GlStateManager.rotate(in[1] == in[0] ? 270 : (float) Math.toDegrees(Math.atan((in[3] - in[2]) / (in[1] - in[0]))), 0, -1, 0);
                    GlStateManager.rotate((float) Math.toDegrees(Math.atan((in[5] - in[4]) / cache.getXZlen())), 0, 0, -1);

                    RenderGlobal.drawSelectionBoundingBox(aabb, 0,0,0,0.4F);

                    GlStateManager.popMatrix();

                    GlStateManager.depthMask(false);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();


                }
            }
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        double hitDist = Double.MAX_VALUE;
        RayTraceResult resultOut = null;
        for (Chunk chunk : this.getOutlines(worldIn, pos)) {

            Connection.Cache cache = chunk.getConnection().getCache(chunk.connectionID);
            double[] in = cache.getIn();

            double yang = in[1] == in[0] ? Math.PI*1.5D : Math.atan((in[3] - in[2]) / (in[1] - in[0]));
            Matrix4d rotymat = new Matrix4d();
            rotymat.rotY(yang);
            Matrix4d invertroty = new Matrix4d();
            invertroty.rotY(-yang);

            double zang = Math.atan((in[5] - in[4]) / cache.getXZlen());
            Matrix4d rotzmat = new Matrix4d();
            rotzmat.rotZ(zang);
            Matrix4d invertrotz = new Matrix4d();
            invertrotz.rotZ(-zang);

            Vector3d startvec = new Vector3d(start.x - in[0], start.y - in[4], start.z - in[2]);
            Vector3d endvec = new Vector3d(end.x - in[0], end.y - in[4], end.z - in[2]);


            rotymat.transform(startvec);
            rotzmat.transform(startvec);

            rotymat.transform(endvec);
            rotzmat.transform(endvec);

            Vec3d sv = new Vec3d(startvec.x, startvec.y, startvec.z);
            Vec3d ev = new Vec3d(endvec.x, endvec.y, endvec.z);

            Vec3d diff = sv.subtract(ev);

            //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
            sv = sv.addVector(diff.x*100, diff.y*100, diff.z*100);
            ev = ev.subtract(diff.x*100, diff.y*100, diff.z*100);

            RayTraceResult result = this.rayTrace(BlockPos.ORIGIN, sv, ev, chunk.aabb);
            if(result != null ) {
                double dist = result.hitVec.squareDistanceTo(startvec.x, startvec.y, startvec.z);

                if(dist < hitDist) {
                    Vector3d hitvec = new Vector3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
                    invertrotz.transform(hitvec);
                    invertroty.transform(hitvec);

                    Vec3i vec = result.sideHit.getDirectionVec();
                    Vector3d sidevec = new Vector3d(vec.getX(), vec.getY(), vec.getZ());
                    invertrotz.transform(sidevec);
                    invertroty.transform(sidevec);

                    resultOut = new RayTraceResult(new Vec3d(hitvec.x, hitvec.y, hitvec.z), EnumFacing.getFacingFromVector((float) sidevec.x, (float) sidevec.y, (float) sidevec.z), pos);
                    resultOut.hitInfo = chunk;

                    hitDist = dist;
                }
            }
        }
        return resultOut;
    }

    public void placeBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            BlockEntityElectricFence be = (BlockEntityElectricFence) te;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if(x == 0 && y == 0 && z == 0) {
                            continue;
                        }
                        TileEntity tileentity = worldIn.getTileEntity(pos.add(x, y, z));
                        if(tileentity instanceof ConnectableBlockEntity) {
                            ConnectableBlockEntity cbe = (ConnectableBlockEntity) tileentity;
                            for (Connection connection : cbe.getConnections()) {
                                if(connection.getNext().equals(pos) || connection.getPrevious().equals(pos)) {
                                    List<BlockPos> positions = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getType());
                                    for (int i = 0; i < positions.size(); i++) {
                                        if(positions.get(i).equals(pos)) {
                                            Connection newcon = new Connection(connection.getType(), connection.getFrom(), connection.getTo(), positions.get(i-1), positions.get(i+1), pos);
//                                            if(side == EnumFacing.UP) {
//                                                for (int i1 = 0; i1 < newcon.getType().getOffsets().length - 1; i1++) {
//                                                    newcon.breakIndex(i1);
//                                                }
//                                            } else {
//                                                for (int i1 = 1; i1 < newcon.getType().getOffsets().length; i1++) {
//                                                    newcon.breakIndex(i1);
//                                                }
//                                            }
                                            be.addConnection(newcon);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
        if(result != null && result.hitInfo instanceof Chunk) {
            ((Chunk) result.hitInfo).connection.breakIndex(((Chunk) result.hitInfo).connectionID);
            TileEntity te = world.getTileEntity(pos);
            if(te != null) {
                te.markDirty();
            }
            for (boolean b : ((Chunk) result.hitInfo).connection.getIndexs()) {
                if(b) {
                    return false;
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return super.addDestroyEffects(world, pos, manager);
    }

    private Set<Chunk> getOutlines(World world, BlockPos pos) {
        Set<Chunk> set = Sets.newLinkedHashSet();
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof BlockEntityElectricFence) {
            for (Connection connection : ((BlockEntityElectricFence) tileEntity).fenceConnections) {
                for (int i = 0; i < connection.getType().getOffsets().length; i++) {
                    if(connection.getIndexs()[i]) {
                        double[] in = LineUtils.intersect(pos, connection.getFrom(), connection.getTo(), connection.getType().getOffsets()[i]);
                        if(connection.getCache(i) == null) continue;
                        double w = connection.getCache(i).getFullThick();
                        if(in != null) {
                            set.add(new Chunk(new AxisAlignedBB(0,-w,-w, -connection.getCache(i).getFullLen(), w, w), connection, i));
                        }
                    }
                }
            }
        }
        return set;
    }

    private static boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;

            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;

            boolean set = false;

            for (Connection connection : ((BlockEntityElectricFence) te).fenceConnections) {
                for (int i = 0; i < connection.getType().getOffsets().length; i++) {
                    if(connection.getIndexs()[i]) {
                        double[] in = LineUtils.intersect(pos, connection.getFrom(), connection.getTo(), connection.getType().getOffsets()[i]);
                        if(in != null) {
                            set = true;

                            minX = Math.min(minX, in[0]);
                            maxX = Math.max(maxX, in[1]);

                            minZ = Math.min(minZ, in[2]);
                            maxZ = Math.max(maxZ, in[3]);

                            minY = Math.min(minY, in[4]);
                            maxY = Math.max(maxY, in[5]);
                        }
                    }
                }
            }
            if(set) {
                return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(1/16F);
            }
        }
        return super.getSelectedBoundingBox(state, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            for (AxisAlignedBB bb : ((BlockEntityElectricFence) te).createBoundingBox()) {
                addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
            }
            return;
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(playerIn, 7);
        if(result != null && result.hitInfo instanceof Chunk) {
            Chunk chunk = (Chunk) result.hitInfo;
            Connection con = chunk.connection;
            if(playerIn.getHeldItem(hand).getItem() == Item.getItemFromBlock(this)) {
                int downID = chunk.connectionID-1;
                int upID = chunk.connectionID+1;
                if(facing == EnumFacing.DOWN && downID >= 0 && !con.getIndexs()[downID]) {
                    con.placeIndex(downID);
                    return true;
                } else if(facing == EnumFacing.UP && upID < con.getType().getOffsets().length && !con.getIndexs()[upID]) {
                    con.placeIndex(upID);
                    return true;
                }
                return false;
            }

            Map<Integer, Boolean> map = con.getSignMap();
            map.put(chunk.connectionID, !map.getOrDefault(chunk.connectionID, false));

        }
        TileEntity te = worldIn.getTileEntity(pos);
        if(te != null) {
            te.markDirty();
        }
        return true;
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

    @Value
    public class Chunk {AxisAlignedBB aabb; Connection connection; int connectionID;}

    @Override
    public Item createItem() {
        return new FenceItemBlock();
    }

    public class FenceItemBlock extends ItemBlock {

        public FenceItemBlock() {
            super(BlockElectricFence.this);
        }

        @Override
        public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
            boolean out = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
            if(world.getBlockState(pos).getBlock() == this.block) {
                BlockElectricFence.this.placeBlockOnSide(world, pos, side);
            }
            return out;
        }
    }

}