package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ProjectNublar.MODID)
public class BlockConnectableBase extends Block {

    public static final boolean DEBUG = false;

    //Set this at your own will, just remember to set it back to true after collection
    public static boolean collidableClient = true;
    public static boolean collidableServer = true;

    public BlockConnectableBase(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockConnectableBase(Material materialIn) {
        super(materialIn);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if(worldIn.isRemote ? collidableClient : collidableServer) {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof ConnectableBlockEntity) {
                for (AxisAlignedBB bb : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
                }
                return;
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
        if (te instanceof ConnectableBlockEntity) {
            AxisAlignedBB enityxzbox = entityBox.grow(0.025D, 0, 0.025D);
            for (ConnectionAxisAlignedBB boxIn : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
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
        if(te instanceof ConnectableBlockEntity && entityIn instanceof EntityLivingBase) {
            entityBox = entityBox.grow(0.1D);
            for (ConnectionAxisAlignedBB box : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                if (entityBox.intersects(box.offset(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vector3d vec = new Vector3d((entityBox.maxX+entityBox.minX)/2, (entityBox.maxY+entityBox.minY)/2, (entityBox.maxZ+entityBox.minZ)/2);
                    vec.sub(box.getConnection().getCenter());
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
            if(target.typeOfHit == RayTraceResult.Type.BLOCK && target.hitInfo instanceof BlockConnectableBase.HitChunk) {
                World world = Minecraft.getMinecraft().world;
                BlockPos pos = target.getBlockPos();
                EntityPlayer player = Minecraft.getMinecraft().player;
                IBlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof BlockConnectableBase) {
                    BlockConnectableBase.HitChunk chunk = (BlockConnectableBase.HitChunk) target.hitInfo;
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

                    Connection connection = chunk.getConnection();
                    double[] in = connection.getIn();

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-d3, -d4, -d5);

                    if(DEBUG) {
                        chunk.getResult().debugRender();
                    }

                    GlStateManager.translate(in[0], in[4], in[2]);

                    boolean pb = connection.brokenSide(world, connection.getPrevious());
                    boolean nb = connection.brokenSide(world, connection.getNext());

                    if(connection.getCompared() > 0) {
                        boolean ref = pb;
                        pb = nb;
                        nb = ref;
                    }

                    if(nb || pb) {
                        Vec3d center = chunk.getAabb().getCenter();
                        double ycent = (chunk.getAabb().maxY - chunk.getAabb().minY) / 2;
                        double zcent = (chunk.getAabb().maxZ - chunk.getAabb().minZ) / 2;

                        if(nb) {
                            Vector3d[] bases = connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent));
                            for (int i = 0; i < 4; i++) {
                                bases[i + 4].add(connection.getNextCache().getPoint());
                            }
                            RenderUtils.renderBoxLines(bases, EnumFacing.SOUTH);
                        }
                        if(pb) {
                            Vector3d[] bases = connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent));
                            for (int i = 0; i < 4; i++) {
                                bases[i + 4].sub(connection.getPrevCache().getPoint());
                            }
                            RenderUtils.renderBoxLines(bases, EnumFacing.SOUTH);
                        }
                        if(nb != pb) {
                            if(nb) {
                                RenderUtils.renderBoxLines(connection.getRayBox().points(new AxisAlignedBB(chunk.getAabb().minX, chunk.getAabb().minY, chunk.getAabb().minZ, center.x, center.y + ycent, center.z + zcent)), EnumFacing.NORTH);
                            } else {
                                RenderUtils.renderBoxLines(connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, chunk.getAabb().maxX, chunk.getAabb().maxY, chunk.getAabb().maxZ)), EnumFacing.SOUTH);
                            }
                        }
                    } else {
                        RenderUtils.renderBoxLines(connection.getRayBox().points());
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
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double minZ = Double.MAX_VALUE;

            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            double maxZ = -Double.MAX_VALUE;

            boolean set = false;

            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                if(!connection.isBroken()) {
                    double[] in = connection.getIn();
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

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        double hitDist = Double.MAX_VALUE;
        RayTraceResult resultOut = null;
        Set<BlockConnectableBase.ChunkedInfo> set = this.getOutlines(worldIn, pos);
        if(set.isEmpty()) {
            return this.rayTrace(pos, start, end, FULL_BLOCK_AABB);
        }
        for (BlockConnectableBase.ChunkedInfo chunk : set) {
            Connection connection = chunk.getConnection();
            boolean pb = connection.brokenSide(worldIn, connection.getPrevious());
            boolean nb = connection.brokenSide(worldIn, connection.getNext());

            if(connection.getCompared() > 0) {
                boolean ref = pb;
                pb = nb;
                nb = ref;
            }


            List<RotatedRayBox.Result> results = Lists.newArrayList();
            if(nb || pb) {
                if(nb) {
                    results.add(connection.getNextCache().getRotatedBox().rayTrace(start, end));
                    if(!pb) {
                        results.add(connection.getNextCache().getFixedBox().rayTrace(start, end));
                    }
                }

                if(pb) {
                    results.add(connection.getPrevCache().getRotatedBox().rayTrace(start, end));
                    if(!nb) {
                        results.add(connection.getPrevCache().getFixedBox().rayTrace(start, end));
                    }
                }

            } else {
                results.add(connection.getRayBox().rayTrace(start, end));
            }

            if(!results.isEmpty()) {
                for (RotatedRayBox.Result result : results) {
                    if(result == null) {
                        continue;
                    }
                    double dist = result.getDistance();
                    if(dist < hitDist) {
                        resultOut = new RayTraceResult(result.getResult().hitVec, result.getResult().sideHit, pos);
                        resultOut.hitInfo = new BlockConnectableBase.HitChunk(chunk.getAabb(), chunk.getConnection(), result.getHitDir(), result);

                        hitDist = dist;
                    }
                }
            }
        }
        return resultOut;
    }


    public Set<ChunkedInfo> getOutlines(World world, BlockPos pos) {
        Set<ChunkedInfo> set = Sets.newLinkedHashSet();
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) tileEntity).getConnections()) {
                if(!connection.isBroken()) {
                    double w = connection.getType().getCableWidth();
                    set.add(new ChunkedInfo(new AxisAlignedBB(0,-w,-w, -connection.getFullLen(), w, w), connection));

                }
            }
        }
        return set;
    }

    public List<ConnectionAxisAlignedBB> createBoundingBox(Set<Connection> fenceConnections, BlockPos pos) {
        List<ConnectionAxisAlignedBB> out = Lists.newArrayList();
        for (Connection connection : fenceConnections) {
            double[] intersect = connection.getIn();
            double amount = 16;

            double x = (intersect[1] - intersect[0]) / amount;
            double y = (intersect[5] - intersect[4]) / amount;
            double z = (intersect[3] - intersect[2]) / amount;

            for (int i = 0; i < amount; i++) {
                int next = i + 1;
                out.add(new ConnectionAxisAlignedBB(new AxisAlignedBB(x * i, y * i, z * i, x * next, y * next, z * next).offset(intersect[0] - pos.getX(), intersect[4] - pos.getY(), intersect[2] - pos.getZ()).grow(0,  connection.getType().getCableWidth()/2D, 0), connection));
            }
        }
        return out;
    }


    @Getter
    public class ConnectionAxisAlignedBB extends AxisAlignedBB {

        private final Connection connection;

        public ConnectionAxisAlignedBB(AxisAlignedBB aabb, Connection connection) {
            super(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            this.connection = connection;
        }
    }

    public static void placeEffect(EntityPlayer player, EnumHand hand, World worldIn, BlockPos pos) {
        if(player != null) {
            player.swingArm(hand);
            if(!player.isCreative()) {
                player.getHeldItem(hand).shrink(1);
            }
        }
        SoundType soundType = BlockHandler.ELECTRIC_FENCE.getSoundType();
        worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    public static void breakEffect(World worldIn, BlockPos pos) {
        worldIn.playEvent(2001, pos, Block.getStateId(worldIn.getBlockState(pos)));
    }


    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(player, 7);
        if(result != null && result.hitInfo instanceof BlockConnectableBase.HitChunk) {
            ((BlockConnectableBase.HitChunk) result.hitInfo).getConnection().setBroken(true);
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        RayTraceResult result = ForgeHooks.rayTraceEyes(playerIn, 7);
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof ConnectableBlockEntity) {
            ConnectableBlockEntity be = (ConnectableBlockEntity) te;
            if(result != null && result.hitInfo instanceof HitChunk) {
                HitChunk chunk = (HitChunk) result.hitInfo;
                Connection con = chunk.getConnection();
                double off = chunk.getConnection().getFrom().getY() + chunk.getConnection().getOffset();
                if(chunk.getDir().getAxis() == EnumFacing.Axis.Y) {
                    Connection ref = null;
                    double yref = chunk.getDir() == EnumFacing.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    for (Connection connection : be.getConnections()) {
                        double yoff = connection.getOffset() + connection.getFrom().getY();
                        if(chunk.getDir() == EnumFacing.DOWN) {
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
                } else if(chunk.getDir().getAxis() == EnumFacing.Axis.X) {
                    BlockPos nextPos = chunk.getDir() == EnumFacing.WEST == chunk.getConnection().getCompared() < 0 ? con.getPrevious() : con.getNext();
                    TileEntity nextTe = worldIn.getTileEntity(nextPos);
                    if(!(nextTe instanceof ConnectableBlockEntity)) {
                        if(worldIn.getBlockState(nextPos).getBlock().isReplaceable(worldIn, nextPos)) {
                            worldIn.setBlockState(nextPos, this.getDefaultState());
                            nextTe = worldIn.getTileEntity(nextPos);
                            if(nextTe instanceof ConnectableBlockEntity && generateConnections(worldIn, nextPos, (ConnectableBlockEntity) nextTe, chunk, null)) {
                                placeEffect(playerIn, hand, worldIn, pos);
                            }

                        }
                    }
                    if(nextTe instanceof ConnectableBlockEntity) {
                        for (Connection connection : ((ConnectableBlockEntity) nextTe).getConnections()) {
                            if(connection.lazyEquals(chunk.getConnection())) {
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

    public static boolean generateConnections(World worldIn, BlockPos pos, ConnectableBlockEntity be, @Nullable HitChunk chunk, @Nullable EnumFacing side) {
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
                                        Connection con = new Connection(worldIn, connection.getType(), connection.getOffset(), connection.getFrom(), connection.getTo(), positions.get(Math.max(i - 1, 0)), positions.get(Math.min(i + 1, positions.size() - 1)), pos);
                                        double[] in = con.getIn();
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

        if(chunk != null) {
            EnumFacing face = chunk.getDir();
            if(chunk.getConnection().getCompared() < 0) {
                face = face.getOpposite();
            }
            if(face.getAxis() == EnumFacing.Axis.X) {
                for (Connection connection : newConnections) {
                    if(chunk.getConnection().lazyEquals(connection)) {
                        ref = connection;
                    }
                }
            }

        }
        for (Connection connection : newConnections) {
            connection.setBroken(!connection.lazyEquals(ref));
            be.addConnection(connection);
        }
        if(be instanceof TileEntity) {
            ((TileEntity)be).markDirty();
        }
        return ref != null;
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
                            double[] in = connection.getIn();
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

    @Value
    public static class HitChunk {AxisAlignedBB aabb; Connection connection; EnumFacing dir; RotatedRayBox.Result result;}

    @Value public static class ChunkedInfo {AxisAlignedBB aabb; Connection connection;}
}
