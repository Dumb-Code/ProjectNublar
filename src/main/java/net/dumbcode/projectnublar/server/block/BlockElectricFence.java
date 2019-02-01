package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Sets;
import lombok.Value;
import net.dumbcode.projectnublar.client.utils.DebugUtil;
import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.entity.DamageSourceHandler;
import net.dumbcode.projectnublar.server.particles.ParticleType;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.dumbcode.projectnublar.server.utils.RotatedRayBox;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class BlockElectricFence extends Block implements IItemBlock {

    public static final boolean DEBUG = false;

    public static final int ITEM_FOLD = 20;


    //Set this at your own will, just remember to set it back to true after collection
    public static boolean collidableClient = true;
    public static boolean collidableServer = true;

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
        EnumFacing side = event.getFace();
        if(side != null && !event.getItemStack().isEmpty() && event.getItemStack().getItem() == Item.getItemFromBlock(BlockHandler.ELECTRIC_FENCE)) {
            TileEntity tile = world.getTileEntity(event.getPos().offset(side));
            if(tile instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity cb = (ConnectableBlockEntity) tile;
                if(side.getAxis() == EnumFacing.Axis.Y) {
                    double yRef = side == EnumFacing.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    Connection ref = null;
                    for (Connection connection : cb.getConnections()) {
                        if(connection.isBroken()) {
                            Connection.Cache cache = connection.getCache();
                            double[] in = cache.getIn();
                            double yin = (in[4] + in[5]) / 2D;
                            if (side == EnumFacing.DOWN == yin > yRef) {
                                yRef = yin;
                                ref = connection;
                            }
                        }
                    }
                    if(ref != null) {
                        ref.setBroken(false);
                        event.setCanceled(true);
                        placeEffect(event.getEntityPlayer(), event.getHand(), event.getWorld(), event.getPos());
                    }
                } else {
                    for (Connection connection : cb.getConnections()) {
                        if(connection.isBroken()) {
                            connection.setBroken(false);
                            event.setCanceled(true);
                            placeEffect(event.getEntityPlayer(), event.getHand(), event.getWorld(), event.getPos());
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        if(!ForgeModContainer.fullBoundingBoxLadders) { //TODO: add a config to not do this yeah
            ForgeModContainer.fullBoundingBoxLadders = true;
        }
        TileEntity te = world.getTileEntity(pos);
        AxisAlignedBB entityBox = entity.getCollisionBoundingBox();
        if (entityBox == null) {
            entityBox = entity.getEntityBoundingBox();
        }
        boolean intersect = false;
        if (te instanceof BlockEntityElectricFence) {
            AxisAlignedBB enityxzbox = entityBox.grow(0.025D, 0, 0.025D);
            BlockEntityElectricFence cb = (BlockEntityElectricFence) te;
            for (BlockEntityElectricFence.ConnectionAxisAlignedBB boxIn : cb.createBoundingBox()) {
                AxisAlignedBB box = boxIn.offset(pos);
                if (enityxzbox.intersects(box) && (!entityBox.grow(0, 0.025D, 0).intersects(box) || !entityBox.grow(0, -0.025D, 0).intersects(box))) {
                    intersect = true;
                    if(boxIn.getConnection().isPowered(world)) {
                        return false;
                    }
                }
            }
        }
        return intersect;
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        TileEntity te = worldIn.getTileEntity(pos);
        AxisAlignedBB entityBox = entityIn.getCollisionBoundingBox();
        if(entityBox == null) {
            entityBox = entityIn.getEntityBoundingBox();
        }
        if(te instanceof BlockEntityElectricFence && entityIn instanceof EntityLivingBase) {
            entityBox = entityBox.grow(0.1D);
            BlockEntityElectricFence cb = (BlockEntityElectricFence) te;
            for (BlockEntityElectricFence.ConnectionAxisAlignedBB box : cb.createBoundingBox()) {
                if (entityBox.intersects(box.offset(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vector3d vec = new Vector3d((entityBox.maxX+entityBox.minX)/2, (entityBox.maxY+entityBox.minY)/2, (entityBox.maxZ+entityBox.minZ)/2);
                    vec.sub(box.getConnection().getCache().getCenter());
                    vec.normalize();

                    Vec3d center = box.offset(pos).getCenter();
                    Vec3d other = entityBox.getCenter();
                    if(!worldIn.isRemote) {

                        entityIn.attackEntityFrom(DamageSourceHandler.FENCE_ELECTRIC, 1F);

                        int times = 3;
                        for (int i = 0; i < times; i++) {
                            for (int i1 = 0; i1 < 5; i1++) {
                                ProjectNublar.spawnParticles(ParticleType.SPARKS, worldIn,
                                        center.x + (other.x - center.x) * worldIn.rand.nextGaussian() * 0.5F,
                                        center.y + (other.y - center.y) * worldIn.rand.nextGaussian() * 0.5F,
                                        center.z + (other.z - center.z) * worldIn.rand.nextGaussian() * 0.5F,


                                        worldIn.rand.nextGaussian() * 1.5F,
                                        worldIn.rand.nextGaussian() * 1.5F,
                                        worldIn.rand.nextGaussian() * 1.5F, 3);
                            }
                        }
                    }


                    if(!entityIn.onGround) {
                        vec.scale(0.4D);
                    }

                    entityIn.motionX = vec.x;
                    entityIn.motionY = vec.y * 0.3D;
                    entityIn.motionZ = vec.z;

                    break;
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onDrawBlock(DrawBlockHighlightEvent event) {
        if(event.getSubID() == 0) {
            RayTraceResult target = event.getTarget();
            if(target.typeOfHit == RayTraceResult.Type.BLOCK && target.hitInfo instanceof HitChunk) {
                World world = Minecraft.getMinecraft().world;
                BlockPos pos = target.getBlockPos();
                EntityPlayer player = Minecraft.getMinecraft().player;
                IBlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof BlockElectricFence) {
                    HitChunk chunk = (HitChunk) target.hitInfo;
                    event.setCanceled(true);
                    double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
                    double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
                    double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();

                    GlStateManager.enableBlend();
                    GlStateManager.enableAlpha();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.glLineWidth(2.0F);
                    GlStateManager.disableTexture2D();
                    GlStateManager.depthMask(false);

                    Connection.Cache cache = chunk.connection.getCache();
                    Connection connection = chunk.connection;
                    double[] in = cache.getIn();

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-d3, -d4, -d5);
                    GlStateManager.translate(in[0], in[4], in[2]);

                    if(DEBUG) {
                        DebugUtil.renderFenceCollision(event);
                    }

                    boolean pb = connection.brokenSide(world, connection.getPrevious());
                    boolean nb = connection.brokenSide(world, connection.getNext());

                    if(connection.getCompared() > 0) {
                        boolean ref = pb;
                        pb = nb;
                        nb = ref;
                    }

                    if(nb || pb) {
                        Vec3d center = chunk.aabb.getCenter();
                        double ycent = (chunk.aabb.maxY - chunk.aabb.minY) / 2;
                        double zcent = (chunk.aabb.maxZ - chunk.aabb.minZ) / 2;

                        if(nb) {
                            Vector3d[] bases = cache.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent));
                            for (int i = 0; i < 4; i++) {
                                bases[i + 4].add(cache.getNext().getPoint());
                            }
                            RenderUtils.renderBoxLines(bases, EnumFacing.SOUTH);
                        }
                        if(pb) {
                            Vector3d[] bases = cache.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent));
                            for (int i = 0; i < 4; i++) {
                                bases[i + 4].sub(cache.getPrev().getPoint());
                            }
                            RenderUtils.renderBoxLines(bases, EnumFacing.SOUTH);
                        }
                        if(nb != pb) {
                            if(nb) {
                                RenderUtils.renderBoxLines(cache.getRayBox().points(new AxisAlignedBB(chunk.aabb.minX, chunk.aabb.minY, chunk.aabb.minZ, center.x, center.y + ycent, center.z + zcent)), EnumFacing.NORTH);
                            } else {
                                RenderUtils.renderBoxLines(cache.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, chunk.aabb.maxX, chunk.aabb.maxY, chunk.aabb.maxZ)), EnumFacing.SOUTH);
                            }
                        }
                    } else {
                        RenderUtils.renderBoxLines(cache.getRayBox().points());
                    }


                    GlStateManager.popMatrix();

                    GlStateManager.depthMask(false);
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();


                }
            }
        }
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

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        double hitDist = Double.MAX_VALUE;
        RayTraceResult resultOut = null;
        Set<ChunkedInfo> set = this.getOutlines(worldIn, pos);
        if(set.isEmpty()) {
            return this.rayTrace(pos, start, end, FULL_BLOCK_AABB);
        }
        for (ChunkedInfo chunk : set) {
            Connection.Cache cache = chunk.getConnection().getCache();
//            double[] in = cache.getIn();
//
//            double yang = in[1] == in[0] ? Math.PI*1.5D : Math.atan((in[3] - in[2]) / (in[1] - in[0]));
//            Matrix4d rotymat = new Matrix4d();
//            rotymat.rotY(yang);
//            Matrix4d invertroty = new Matrix4d();
//            invertroty.rotY(-yang);
//
//            double zang = Math.atan((in[5] - in[4]) / cache.getXZlen());
//            Matrix4d rotzmat = new Matrix4d();
//            rotzmat.rotZ(zang);
//            Matrix4d invertrotz = new Matrix4d();
//            invertrotz.rotZ(-zang);
//
//            Vector3d startvec = new Vector3d(start.x - in[0], start.y - in[4], start.z - in[2]);
//            Vector3d endvec = new Vector3d(end.x - in[0], end.y - in[4], end.z - in[2]);
//
//
//            rotymat.transform(startvec);
//            rotzmat.transform(startvec);
//
//            rotymat.transform(endvec);
//            rotzmat.transform(endvec);
//
//            Vec3d sv = new Vec3d(startvec.x, startvec.y, startvec.z);
//            Vec3d ev = new Vec3d(endvec.x, endvec.y, endvec.z);
//
//            Vec3d diff = sv.subtract(ev);
//
//            //Due to the calculations, the points can appear inside the aabb, meaning the aabb calcualtion is wrong. This is just to extend both points a substantial amount to make it work
//            sv = sv.addVector(diff.x*100, diff.y*100, diff.z*100);
//            ev = ev.subtract(diff.x*100, diff.y*100, diff.z*100);


            RotatedRayBox.Result result = cache.getRayBox().rayTrace(start, end);

            if(result != null) {
                double dist = result.getDistance();
                if(dist < hitDist) {
                    resultOut = new RayTraceResult(result.getResult().hitVec, result.getResult().sideHit, pos);
                    resultOut.hitInfo = new HitChunk(chunk.aabb, chunk.connection, result.getHitDir(), result);

                    hitDist = dist;
                }
            }
        }
        return resultOut;
    }

    public void placeBlockOnSide(World worldIn, RayTraceResult result, BlockPos pos, EnumFacing side) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof BlockEntityElectricFence) {
            BlockEntityElectricFence be = (BlockEntityElectricFence) te;

            Pair<Set<Connection>, Connection> pairResult = generateConnections(worldIn, pos, side);

            Set<Connection> newConnections = pairResult.getLeft();
            Connection ref = pairResult.getRight();

            if(result != null && result.hitInfo instanceof HitChunk) {
                HitChunk hc = (HitChunk) result.hitInfo;
                EnumFacing face = hc.dir;
                if(hc.connection.getCompared() < 0) {
                    face = face.getOpposite();
                }
                if(face.getAxis() == EnumFacing.Axis.X) {
                    for (Connection connection : newConnections) {
                        if(hc.connection.lazyEquals(connection)) {
                            ref = connection;
                        }
                    }
                }
            }
            for (Connection connection : newConnections) {
                connection.setBroken(!connection.lazyEquals(ref));
                be.addConnection(connection);
            }
            te.markDirty();

        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
        if(result != null && result.hitInfo instanceof HitChunk) {
            ((HitChunk) result.hitInfo).connection.setBroken(true);
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof ConnectableBlockEntity) {
                for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                    if(!connection.isBroken()) {
                        breakEffect(world, pos);
                        return false;
                    }
                }
                te.markDirty();
            }

        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }


    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return super.isReplaceable(worldIn, pos);
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return super.addDestroyEffects(world, pos, manager);
    }

    private Set<ChunkedInfo> getOutlines(World world, BlockPos pos) {
        Set<ChunkedInfo> set = Sets.newLinkedHashSet();
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof BlockEntityElectricFence) {
            for (Connection connection : ((BlockEntityElectricFence) tileEntity).fenceConnections) {
                if(!connection.isBroken()) {
                    double w = connection.getCache().getFullThick();
                    set.add(new ChunkedInfo(new AxisAlignedBB(0,-w,-w, -connection.getCache().getFullLen(), w, w), connection));

                }
            }
        }
        return set;
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
                if(!connection.isBroken()) {
                    Connection.Cache cache = connection.getCache();
                    double[] in = cache.getIn();
                    set = true;

                    minX = Math.min(minX, in[0]);
                    maxX = Math.max(maxX, in[1]);

                    minZ = Math.min(minZ, in[2]);
                    maxZ = Math.max(maxZ, in[3]);

                    minY = Math.min(minY, in[4]);
                    maxY = Math.max(maxY, in[5]);
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
        if(worldIn.isRemote ? collidableClient : collidableServer) {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof BlockEntityElectricFence) {
                for (AxisAlignedBB bb : ((BlockEntityElectricFence) te).createBoundingBox()) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
                }
                return;
            }
        }
        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(playerIn, 7);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            ConnectableBlockEntity be = (ConnectableBlockEntity) te;
            if(result != null && result.hitInfo instanceof HitChunk) {
                HitChunk chunk = (HitChunk) result.hitInfo;
                Connection con = chunk.connection;
                double off = chunk.connection.getFrom().getY() + chunk.connection.getOffset();
                if(chunk.dir.getAxis() == EnumFacing.Axis.Y) {
                    Connection ref = null;
                    double yref = chunk.dir == EnumFacing.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    for (Connection connection : be.getConnections()) {
                        double yoff = connection.getOffset() + connection.getFrom().getY();
                        if(chunk.dir == EnumFacing.DOWN) {
                            if(yoff < off && yoff > yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        } else {
                            if(yoff > off && yoff < yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        }
                    }
                    if(ref != null && ref.isBroken()) {
                        ref.setBroken(false);
                        te.markDirty();
                        placeEffect(playerIn, hand, worldIn, pos);
                        return true;
                    }
                } else if(chunk.dir.getAxis() == EnumFacing.Axis.X) {
                    BlockPos nextPos = chunk.dir == EnumFacing.WEST == chunk.connection.getCompared() < 0 ? con.getPrevious() : con.getNext();
                    TileEntity nextTe = worldIn.getTileEntity(nextPos);
                    if(!(nextTe instanceof ConnectableBlockEntity)) {
                        if(worldIn.getBlockState(nextPos).getBlock().isReplaceable(worldIn, nextPos)) {
                            worldIn.setBlockState(nextPos, this.getDefaultState());
                            nextTe = worldIn.getTileEntity(nextPos);
                            generateConnections(worldIn, nextPos, null);
                        }
                    }
                    if(nextTe instanceof ConnectableBlockEntity) {
                        for (Connection connection : ((ConnectableBlockEntity) nextTe).getConnections()) {
                            if(connection.lazyEquals(chunk.connection)) {
                                connection.setBroken(false);
                                placeEffect(playerIn, hand, worldIn, pos);
                                nextTe.markDirty();
                                return true;
                            }
                        }
                    }
                }
                if(playerIn.getHeldItem(hand).getItem() == Item.getItemFromBlock(this)) {
                    return false;
                }
                con.setSign(!con.isSign());
                te.markDirty();
            }

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

    public static void placeEffect(EntityPlayer player, EnumHand hand, World worldIn, BlockPos pos) {
        if(player != null) {
            player.swingArm(hand);
            if(!player.isCreative()) {
                player.getHeldItem(hand).shrink(1);
            }
        }
        SoundType soundType = BlockHandler.ELECTRIC_FENCE.blockSoundType;
        worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    public static void breakEffect(World worldIn, BlockPos pos) {
        worldIn.playEvent(2001, pos, Block.getStateId(worldIn.getBlockState(pos)));
    }

    @Value public class HitChunk {AxisAlignedBB aabb; Connection connection; EnumFacing dir; RotatedRayBox.Result boxResult;}

    @Value public class ChunkedInfo {AxisAlignedBB aabb; Connection connection;}


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
            if(result != null && result.hitInfo instanceof HitChunk) {
                HitChunk chunk = (HitChunk) result.hitInfo;
                EnumFacing dir = chunk.dir;
                //Make sure that if its placed on the east/west side (the ends of the cables) to place the block on the previous/next positions
                if(dir == EnumFacing.EAST) {
                    pos = chunk.connection.getPrevious();
                } else if(dir == EnumFacing.WEST) {
                    pos = chunk.connection.getNext();
                }
            }
            boolean out = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
            if(world.getBlockState(pos).getBlock() == this.block) {
                BlockElectricFence.this.placeBlockOnSide(world, result, pos, side);
            }
            return out;
        }
    }

    public static Pair<Set<Connection>, Connection> generateConnections(World worldIn, BlockPos pos, @Nullable EnumFacing side) {
        Set<Connection> newConnections = Sets.newLinkedHashSet();
        double yRef = side == EnumFacing.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection ref = null;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    TileEntity tileentity = worldIn.getTileEntity(pos.add(x, y, z));
                    if (tileentity instanceof ConnectableBlockEntity) {
                        ConnectableBlockEntity cbe = (ConnectableBlockEntity) tileentity;
                        for (Connection connection : cbe.getConnections()) {
                            if (connection.getNext().equals(pos) || connection.getPrevious().equals(pos)) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset());
                                for (int i = 0; i < positions.size(); i++) {
                                    if (positions.get(i).equals(pos)) {
                                        Connection con = new Connection(connection.getType(), connection.getOffset(), connection.getFrom(), connection.getTo(), positions.get(Math.max(i - 1, 0)), positions.get(Math.min(i + 1, positions.size() - 1)), pos);
                                        Connection.Cache cache = con.getCache();
                                        double[] in = cache.getIn();
                                        double yin = (in[4] + in[5]) / 2D;
                                        if (side == EnumFacing.DOWN == yin > yRef) {
                                            yRef = yin;
                                            ref = con;
                                        }
                                        newConnections.add(con);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return Pair.of(newConnections, ref);
    }

}
