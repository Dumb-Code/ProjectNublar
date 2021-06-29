package net.dumbcode.projectnublar.server.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.dumbcode.projectnublar.server.entity.DamageSourceHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.particles.ProjectNublarParticles;
import net.dumbcode.projectnublar.server.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class BlockConnectableBase extends Block {

    //Set this at your own will, just remember to set it back to true after collection
    private static boolean collidableClient = true;
    private static boolean collidableServer = true;

    public BlockConnectableBase(Properties properties) {
        super(properties);
    }


    @Override
    public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity) {
        if (!ForgeConfig.SERVER.fullBoundingBoxLadders.get()) {
            ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
        }
        TileEntity te = world.getBlockEntity(pos);
        AxisAlignedBB entityBox = entity.getBoundingBox();
        boolean intersect = false;
        if (te instanceof ConnectableBlockEntity) {
            AxisAlignedBB enityxzbox = entityBox.inflate(0.025D, 0, 0.025D);
            for (ConnectionAxisAlignedBB boxIn : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                AxisAlignedBB box = boxIn.move(pos);
                if (enityxzbox.intersects(box) && (!entityBox.inflate(0, 0.025D, 0).intersects(box) || !entityBox.inflate(0, -0.025D, 0).intersects(box))) {
                    intersect = true;
                    if (boxIn.getConnection().isPowered(world)) {
                        return false;
                    }
                }
            }
        }
        return intersect;
    }

    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        TileEntity te = worldIn.getBlockEntity(pos);
        AxisAlignedBB entityBox = entityIn.getBoundingBox();
        if (te instanceof ConnectableBlockEntity) {
            entityBox = entityBox.inflate(0.1D);
            for (ConnectionAxisAlignedBB box : this.createBoundingBox(((ConnectableBlockEntity) te).getConnections(), pos)) {
                if (entityBox.intersects(box.move(pos)) && box.getConnection().isPowered(worldIn)) {

                    Vector3d vec = new Vector3d((entityBox.maxX + entityBox.minX) / 2, (entityBox.maxY + entityBox.minY) / 2, (entityBox.maxZ + entityBox.minZ) / 2);
                    vec.subtract(box.getConnection().getCenter());
                    vec.normalize();

                    Vector3d center = this.center(box.move(pos));
                    Vector3d other = this.center(entityBox);
                    if (worldIn instanceof ServerWorld) {
                        entityIn.hurt(DamageSourceHandler.FENCE_ELECTRIC, 1F);

                        int count = 30;
                        ((ServerWorld) worldIn).sendParticles(ProjectNublarParticles.SPARK.get(),
                            center.x, center.y, center.z, count,
                            (other.x - center.x) * 0.5F,
                            (other.y - center.y) * 0.5F,
                            (other.z - center.z) * 0.5F,
                            1.5F
                        );

                    }

                    if (!entityIn.isOnGround()) {
                        vec = vec.scale(0.4D);
                    }

                    entityIn.setDeltaMovement(new Vector3d(vec.x, vec.y * 0.3D, vec.z));

                    break;
                }
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
        return false;
    }


    private Vector3d center(AxisAlignedBB box) {
        return new Vector3d(box.minX + (box.maxX - box.minX) * 0.5D, box.minY + (box.maxY - box.minY) * 0.5D, box.minZ + (box.maxZ - box.minZ) * 0.5D);
    }

    @SubscribeEvent
    public static void onDrawBlock(DrawHighlightEvent.HighlightBlock event) {
        IRenderTypeBuffer buffers = event.getBuffers();
        MatrixStack stack = event.getMatrix();
        IVertexBuilder buffer = buffers.getBuffer(RenderType.lines());
        BlockRayTraceResult target = event.getTarget();
        ActiveRenderInfo info = event.getInfo();
        Vector3d position = info.getPosition();
        BlockPos pos = target.getBlockPos();
        double px = -position.x();
        double py = -position.y();
        double pz = -position.z();

        if(target.getType() == RayTraceResult.Type.BLOCK && target.hitInfo instanceof DelegateVoxelShapeRender) {
            WorldRenderer.renderShape(stack, buffer, ((DelegateVoxelShapeRender) target.hitInfo).getToRender(), pos.getX()+px, pos.getY()+py, pos.getZ()+pz, 0F, 0F, 0F, 0.4F);
            event.setCanceled(true);
        }
        if (target.getType() == RayTraceResult.Type.BLOCK && target.hitInfo instanceof BlockConnectableBase.HitChunk) {
            World world = Minecraft.getInstance().level;
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockConnectableBase) {
                BlockConnectableBase.HitChunk chunk = (BlockConnectableBase.HitChunk) target.hitInfo;
                event.setCanceled(true);


                Connection connection = chunk.getConnection();
                double[] in = connection.getIn();

                if (ProjectNublar.DEBUG) {
                    chunk.getResult().debugRender(stack, buffers, px, py, pz);
                }


                double x = in[0] - position.x();
                double y = in[4] - position.y();
                double z = in[2] - position.z();


                boolean pb = connection.brokenSide(world, false);
                boolean nb = connection.brokenSide(world, true);

                if (nb || pb) {
                    Vector3d center = chunk.getAabb().getCenter();
                    double ycent = (chunk.getAabb().maxY - chunk.getAabb().minY) / 2;
                    double zcent = (chunk.getAabb().maxZ - chunk.getAabb().minZ) / 2;

                    if (nb) {
                        Vector3f[] bases = connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent), x, y, z);
                        for (int i = 0; i < 4; i++) {
                            bases[i + 4].add(connection.getNextCache().getPoint());
                        }
                        RenderUtils.renderBoxLines(stack, buffer, bases, Direction.SOUTH);
                    }
                    if (pb) {
                        Vector3f[] bases = connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, center.x, center.y + ycent, center.z + zcent), x, y, z);
                        for (int i = 0; i < 4; i++) {
                            bases[i + 4].add(connection.getPrevCache().getPoint());
                        }
                        RenderUtils.renderBoxLines(stack, buffer, bases, Direction.SOUTH);
                    }
                    if (nb != pb) {
                        if (nb) {
                            RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(new AxisAlignedBB(chunk.getAabb().minX, chunk.getAabb().minY, chunk.getAabb().minZ, center.x, center.y + ycent, center.z + zcent), x, y, z), Direction.NORTH);
                        } else {
                            RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(new AxisAlignedBB(center.x, center.y - ycent, center.z - zcent, chunk.getAabb().maxX, chunk.getAabb().maxY, chunk.getAabb().maxZ), x, y, z), Direction.SOUTH);
                        }
                    }
                } else {
                    RenderUtils.renderBoxLines(stack, buffer, connection.getRayBox().points(x, y, z));
                }
            }
        }
    }

    //    @Override
//    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
//        TileEntity te = worldIn.getTileEntity(pos);
//        if(te instanceof ConnectableBlockEntity) {
//            double minX = Double.MAX_VALUE;
//            double minY = Double.MAX_VALUE;
//            double minZ = Double.MAX_VALUE;
//
//            double maxX = -Double.MAX_VALUE;
//            double maxY = -Double.MAX_VALUE;
//            double maxZ = -Double.MAX_VALUE;
//
//            boolean set = false;
//
//            for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
//                if(!connection.isBroken()) {
//                    double[] in = connection.getIn();
//                    set = true;
//
//                    minX = Math.min(minX, in[0]);
//                    maxX = Math.max(maxX, in[1]);
//
//                    minZ = Math.min(minZ, in[2]);
//                    maxZ = Math.max(maxZ, in[3]);
//
//                    minY = Math.min(minY, in[4]);
//                    maxY = Math.max(maxY, in[5]);
//                }
//            }
//            if(set) {
//                return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(1/16F);
//            }
//        }
//        return super.getSelectedBoundingBox(state, worldIn, pos);
//    }

    protected VoxelShape getDefaultShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return this.createDelegateShape(this.estimateShape(world, pos), this.getDefaultShape(state, world, pos, context), world);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (world instanceof ServerWorld ? collidableServer : collidableClient) {
            return this.estimateShape(world, pos);
        }
        return VoxelShapes.empty();
    }

    protected VoxelShape estimateShape(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof ConnectableBlockEntity) {
            return ((ConnectableBlockEntity) te).getOrCreateCollision();
        }
        return VoxelShapes.empty();
    }

    protected VoxelShape createDelegateShape(VoxelShape shape, VoxelShape interactionShape, IBlockReader world) {
        return new DelegateVoxelShape(shape, (from, to, offset, fallback) -> {
            BlockRayTraceResult raytraceResult = getRaytraceResult(world, offset, from, to);
            BlockRayTraceResult defaultResult = interactionShape.clip(from, to, offset);
            if(defaultResult == null) {
                return raytraceResult;
            }
            defaultResult.hitInfo = new DelegateVoxelShapeRender(interactionShape);
            if(raytraceResult == null) {
                return defaultResult;
            }
            return raytraceResult.getLocation().subtract(from).lengthSqr() < defaultResult.getLocation().subtract(from).lengthSqr() ? raytraceResult : defaultResult;
        });
    }

    @Nullable
    public static HitChunk getHitChunk(PlayerEntity viewer) {
        Vector3d start = viewer.getEyePosition(1F);
        Vector3d vec = viewer.getViewVector(1F);
        Vector3d end = start.add(vec.x * 20, vec.y * 20, vec.z * 20);
        RayTraceResult pick = viewer.pick(20, 1F, false);
        if (!(pick instanceof BlockRayTraceResult)) {
            return null;
        }
        BlockRayTraceResult result = getRaytraceResult(viewer.level, ((BlockRayTraceResult) pick).getBlockPos(), start, end);
        if(result != null && result.hitInfo instanceof HitChunk) {
            return (HitChunk) result.hitInfo;
        }
        return null;
    }


    @Nullable
    public static BlockRayTraceResult getRaytraceResult(IBlockReader world, BlockPos pos, Vector3d start, Vector3d end) {
        double hitDist = Double.MAX_VALUE;
        BlockRayTraceResult resultOut = null;
        Set<BlockConnectableBase.ChunkedInfo> set = getOutlines(world, pos);

        for (BlockConnectableBase.ChunkedInfo chunk : set) {
            Connection connection = chunk.getConnection();
            boolean pb = connection.brokenSide(world, false);
            boolean nb = connection.brokenSide(world, true);

            List<RotatedRayBox.Result> results = Lists.newArrayList();
            if (nb || pb) {
                if (nb) {
                    results.add(connection.getNextCache().getRotatedBox().rayTrace(start, end));
                    if (!pb) {
                        results.add(connection.getNextCache().getFixedBox().rayTrace(start, end));
                    }
                }

                if (pb) {
                    results.add(connection.getPrevCache().getRotatedBox().rayTrace(start, end));
                    if (!nb) {
                        results.add(connection.getPrevCache().getFixedBox().rayTrace(start, end));
                    }
                }

            } else {
                results.add(connection.getRayBox().rayTrace(start, end));
            }

            if (!results.isEmpty()) {
                for (RotatedRayBox.Result result : results) {
                    if (result == null) {
                        continue;
                    }
                    double dist = result.getDistance();
                    if (dist < hitDist) {
                        resultOut = result.getResult().withPosition(pos);
                        resultOut.hitInfo = new BlockConnectableBase.HitChunk(chunk.getAabb(), chunk.getConnection(), result.getHitDir(), result);
                        hitDist = dist;
                    }
                }
            }
        }
        return resultOut;
    }

    public static Set<ChunkedInfo> getOutlines(IBlockReader world, BlockPos pos) {
        Set<ChunkedInfo> set = Sets.newLinkedHashSet();
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof ConnectableBlockEntity) {
            for (Connection connection : ((ConnectableBlockEntity) tileEntity).getConnections()) {
                if (!connection.isBroken()) {
                    double w = connection.getType().getCableWidth();
                    set.add(new ChunkedInfo(new AxisAlignedBB(0, -w, -w, -connection.getFullLen(), w, w), connection));

                }
            }
        }
        return set;
    }

    public static List<ConnectionAxisAlignedBB> createBoundingBox(Set<Connection> fenceConnections, BlockPos pos) {
        List<ConnectionAxisAlignedBB> out = Lists.newArrayList();
        for (Connection connection : fenceConnections) {
            double[] intersect = connection.getIn();
            double amount = 8;

            double x = (intersect[1] - intersect[0]) / amount;
            double y = (intersect[5] - intersect[4]) / amount;
            double z = (intersect[3] - intersect[2]) / amount;

            for (int i = 0; i < amount; i++) {
                int next = i + 1;
                out.add(new ConnectionAxisAlignedBB(
                    new AxisAlignedBB(x * i, y * i, z * i, x * next, y * next, z * next)
                        .move(intersect[0] - pos.getX(), intersect[4] - pos.getY(), intersect[2] - pos.getZ())
                        .inflate(0, connection.getType().getCableWidth() / 2D, 0), connection)
                );
            }
        }
        return out;
    }


    @Getter
    public static class ConnectionAxisAlignedBB extends AxisAlignedBB {

        private final Connection connection;

        public ConnectionAxisAlignedBB(AxisAlignedBB aabb, Connection connection) {
            super(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
            this.connection = connection;
        }
    }

    public static void placeEffect(PlayerEntity player, Hand hand, World worldIn, BlockPos pos) {
        if (player != null) {
            player.swing(hand);
            if (!player.isCreative()) {
                player.getItemInHand(hand).shrink(1);
            }
        }
        SoundType soundType = BlockHandler.ELECTRIC_FENCE.get().getSoundType(BlockHandler.ELECTRIC_FENCE.get().defaultBlockState());
        worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
    }

    public static void breakEffect(World worldIn, BlockPos pos) {
        worldIn.levelEvent(2001, pos, Block.getId(worldIn.getBlockState(pos)));
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        HitChunk chunk = getHitChunk(player);
        if (chunk != null) {
            chunk.getConnection().setBroken(true);
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                    if (!connection.isBroken()) {
                        breakEffect(world, pos);
                        return false;
                    }
                }
                te.setChanged();
            }

        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (ray.hitInfo instanceof HitChunk) {
            HitChunk chunk = (HitChunk) ray.hitInfo;
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity be = (ConnectableBlockEntity) te;
                Connection con = chunk.getConnection();
                double off = chunk.getConnection().getFrom().getY() + chunk.getConnection().getOffset();
                if (chunk.getDir().getAxis() == Direction.Axis.Y) {
                    Connection ref = null;
                    double yref = chunk.getDir() == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    for (Connection connection : be.getConnections()) {
                        double yoff = connection.getOffset() + connection.getFrom().getY();
                        if (chunk.getDir() == Direction.DOWN) {
                            if (yoff < off && yoff > yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        } else {
                            if (yoff > off && yoff < yref) {
                                yref = yoff;
                                ref = connection;
                            }
                        }
                    }
                    if (ref != null && ref.isBroken()) {
                        ref.setBroken(false);
                        te.setChanged();
                        placeEffect(player, hand, world, pos);
                        return ActionResultType.SUCCESS;
                    }
                } else if (chunk.getDir().getAxis() == Direction.Axis.X) {
                    BlockPos nextPos = chunk.getDir() == Direction.WEST == chunk.connection.getCompared() < 0 ? con.getNext() : con.getPrevious();
                    TileEntity nextTe = world.getBlockEntity(nextPos);
                    if (!(nextTe instanceof ConnectableBlockEntity)) {
                        if (world.getBlockState(nextPos).canBeReplaced(Fluids.EMPTY)) {
                            world.setBlock(nextPos, this.defaultBlockState(), 3);
                            nextTe = world.getBlockEntity(nextPos);
                            if (nextTe instanceof ConnectableBlockEntity && generateConnections(world, nextPos, (ConnectableBlockEntity) nextTe, chunk, null)) {
                                placeEffect(player, hand, world, pos);
                            }

                        }
                    }
                    if (nextTe instanceof ConnectableBlockEntity) {
                        for (Connection connection : ((ConnectableBlockEntity) nextTe).getConnections()) {
                            if (connection.lazyEquals(chunk.getConnection())) {
                                connection.setBroken(false);
                                placeEffect(player, hand, world, pos);
                                nextTe.setChanged();
                                return ActionResultType.SUCCESS;
                            }
                        }
                    }
                }
                if (player.getItemInHand(hand).getItem() == Item.byBlock(this)) {
                    return ActionResultType.CONSUME;
                }
                con.setSign(!con.isSign());
                te.setChanged();
            }
        }
        return ActionResultType.SUCCESS;
    }


    public static boolean generateConnections(World worldIn, BlockPos pos, ConnectableBlockEntity be, @Nullable HitChunk chunk, @Nullable Direction side) {
        Set<Connection> newConnections = Sets.newLinkedHashSet();
        double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
        Connection ref = null;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    TileEntity tileentity = worldIn.getBlockEntity(pos.offset(x, y, z));
                    if (tileentity instanceof ConnectableBlockEntity) {
                        ConnectableBlockEntity cbe = (ConnectableBlockEntity) tileentity;
                        for (Connection connection : cbe.getConnections()) {
                            if (connection.getPrevious().equals(pos) || connection.getNext().equals(pos)) {
                                List<BlockPos> positions = LineUtils.getBlocksInbetween(connection.getFrom(), connection.getTo(), connection.getOffset());
                                for (int i = 0; i < positions.size(); i++) {
                                    if (positions.get(i).equals(pos)) {
                                        Connection con = new Connection(tileentity, connection.getType(), connection.getOffset(), connection.getFrom(), connection.getTo(), positions.get(Math.min(i + 1, positions.size() - 1)), positions.get(Math.max(i - 1, 0)), pos);
                                        double[] in = con.getIn();
                                        double yin = (in[4] + in[5]) / 2D;
                                        if (side == Direction.DOWN == yin > yRef) {
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

        if (chunk != null) {
            Direction face = chunk.getDir();
            if (chunk.getConnection().getCompared() < 0) {
                face = face.getOpposite();
            }
            if (face.getAxis() == Direction.Axis.X) {
                for (Connection connection : newConnections) {
                    if (chunk.getConnection().lazyEquals(connection)) {
                        ref = connection;
                    }
                }
            }

        }
        for (Connection connection : newConnections) {
            connection.setBroken(!connection.lazyEquals(ref));
            be.addConnection(connection);
        }
        if (be instanceof TileEntity) {
            ((TileEntity) be).setChanged();
        }
        return ref != null;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        Direction side = event.getFace();
        if (side != null && !event.getItemStack().isEmpty() && event.getItemStack().getItem() == ItemHandler.WIRE_SPOOL.get()) {
            TileEntity tile = world.getBlockEntity(event.getPos().relative(side));
            if (tile instanceof ConnectableBlockEntity) {
                ConnectableBlockEntity cb = (ConnectableBlockEntity) tile;
                if (side.getAxis() == Direction.Axis.Y) {
                    double yRef = side == Direction.DOWN ? Double.MIN_VALUE : Double.MAX_VALUE;
                    Connection ref = null;
                    for (Connection connection : cb.getConnections()) {
                        if (connection.isBroken()) {
                            double[] in = connection.getIn();
                            double yin = (in[4] + in[5]) / 2D;
                            if (side == Direction.DOWN == yin > yRef) {
                                yRef = yin;
                                ref = connection;
                            }
                        }
                    }
                    if (ref != null) {
                        ref.setBroken(false);
                        event.setCanceled(true);
                        placeEffect(event.getPlayer(), event.getHand(), event.getWorld(), event.getPos());
                    }
                } else {
                    for (Connection connection : cb.getConnections()) {
                        if (connection.isBroken()) {
                            connection.setBroken(false);
                            event.setCanceled(true);
                            placeEffect(event.getPlayer(), event.getHand(), event.getWorld(), event.getPos());
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void setCollidableClient(boolean client) {
        collidableClient = client;
    }

    public static void setCollidableServer(boolean server) {
        collidableServer = server;
    }

    @Value
    public static class HitChunk {
        AxisAlignedBB aabb;
        Connection connection;
        Direction dir;
        RotatedRayBox.Result result;
    }

    @Value
    public static class ChunkedInfo {
        AxisAlignedBB aabb;
        Connection connection;
    }

    @Value
    public static class DelegateVoxelShapeRender {
        VoxelShape toRender;
    }
}
