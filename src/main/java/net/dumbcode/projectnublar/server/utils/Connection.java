package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.*;
import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector3d;

import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import static lombok.EqualsAndHashCode.Include;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Connection {
    private final boolean client;
    @Include private final ConnectionType type;
    @Include private final double offset;
    //Used to help compare Connections
    @Include private final int toFromHash;
    private final BlockPos from;
    private final BlockPos to;

    private final BlockPos previous;
    private final BlockPos next;

    @Setter boolean sign;

    private final BlockPos position;
    private final int compared;

    private VboCache rendercache;
    @Setter private boolean broken;

    private final double[] in;

    private final double xzlen;
    private final double fullLen;

    private final Random random;

    private final Vector3d center;
    private final RotatedRayBox rayBox;

    private final SurroundingCache prevCache;
    private final SurroundingCache nextCache;

    public Connection(World world, ConnectionType type, double offset, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this.client = world != null && world.isRemote;
        this.type = type;
        this.offset = offset;
        this.position = position;

        if((this.compared = (from.getX() == to.getX() ? to.getZ() - from.getZ() : from.getX() - to.getX())) < 0) {
            BlockPos ref = from;
            from = to;
            to = ref;


        } else { //Somthings gone on here where the prev/next positions are flipped. I should look into why that is but for now this works
            BlockPos ref = previous;
            previous = next;
            next = ref;
        }

        this.from = from;
        this.to = to;
        this.previous = previous;
        this.next = next;

//        this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();

        this.toFromHash = (this.compared < 0 ? this.from : this.to).hashCode() + (this.compared < 0 ? this.to : this.from).hashCode() * 31;

        double[] in = LineUtils.intersect(this.position, from, to, this.offset);
        if(in == null) {
           in = new double[6]; //ew
        }

        this.random = new Random(this.getPosition().toLong() * (long)(this.getOffset() * 1000));
        this.in = in;
        double w = this.type.getCableWidth();
        this.xzlen = Math.sqrt((this.in[1]-this.in[0])*(this.in[1]-this.in[0]) + (this.in[3]-this.in[2])*(this.in[3]-this.in[2]));
        this.fullLen = Math.sqrt(this.xzlen*this.xzlen + (this.in[5]-this.in[4])*(this.in[5]-this.in[4]));

        this.center = new Vector3d((this.in[0]+this.in[1])/2, (this.in[4]+this.in[5])/2, (this.in[2]+this.in[3])/2);
        this.rayBox =  new RotatedRayBox.Builder(new AxisAlignedBB(0, -w, -w, -this.fullLen, w, w))
                .origin(this.in[0], this.in[4], this.in[2])
                .rotate(Math.atan((this.in[5] - this.in[4]) / this.xzlen), 0, 0, 1)
                .rotate(this.in[1] == this.in[0] ? Math.PI*1.5D : Math.atan((this.in[3] - this.in[2]) / (this.in[1] - this.in[0])), 0, 1, 0)
                .build();

        this.prevCache = this.genCache(false);
        this.nextCache = this.genCache(true);

    }

    private SurroundingCache genCache(boolean next) {
        Vector3d point = new Vector3d(this.fullLen/2, 0, 0);
        double w = this.type.getCableWidth();
        Vector3d center = new Vector3d((this.in[0]+this.in[1])/2, (this.in[4]+this.in[5])/2, (this.in[2]+this.in[3])/2);
        RotatedRayBox fixedBox = new RotatedRayBox.Builder(new AxisAlignedBB(0, -w, -w, -this.fullLen/2, w, w))
                .origin(next?center.x:this.in[0], next?center.y:this.in[4], next?center.z:this.in[2])
                .rotate(Math.atan((this.in[5] - this.in[4]) / this.xzlen), 0, 0, 1)
                .rotate(this.in[1] == this.in[0] ? Math.PI*1.5D : Math.atan((this.in[3] - this.in[2]) / (this.in[1] - this.in[0])), 0, 1, 0)
                .build();
        RotatedRayBox rotatedBox = new RotatedRayBox.Builder(new AxisAlignedBB(0, -w, -w, (next ? 1 : -1) * this.fullLen/2, w, w))
                .origin(center.x, center.y, center.z)
                .rotate((this.in[1] == this.in[0] ? Math.PI*1.5D : Math.atan((this.in[3] - this.in[2]) / (this.in[1] - this.in[0]))) + ((this.random.nextFloat()-0.5F) * Math.PI/3F), 0, 1, 0)
                .rotate(Math.atan((this.in[5] - this.in[4]) / this.xzlen) + ((this.random.nextFloat()-0.5F) * Math.PI/3F), 0, 0, 1)
                .build();
        rotatedBox.getBackwards().transform(point);
        return new SurroundingCache(point, fixedBox, rotatedBox);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", this.type.ordinal());
        nbt.setDouble("offset", this.offset);
        nbt.setLong("from", this.getFrom().toLong());
        nbt.setLong("to", this.getTo().toLong());
        nbt.setBoolean("sign", this.sign);
        nbt.setLong("previous", this.previous.toLong());
        nbt.setLong("next", this.next.toLong());
        nbt.setBoolean("broken", this.broken);
        return nbt;
    }

    public static Connection fromNBT(NBTTagCompound nbt, TileEntity tileEntity) {
        Connection c = new Connection(tileEntity.getWorld(), ConnectionType.getType(nbt.getInteger("type")), nbt.getDouble("offset"), BlockPos.fromLong(nbt.getLong("from")), BlockPos.fromLong(nbt.getLong("to")), BlockPos.fromLong(nbt.getLong("previous")), BlockPos.fromLong(nbt.getLong("next")), tileEntity.getPos());
        c.setBroken(nbt.getBoolean("broken"));
        return c;
    }

    public boolean lazyEquals(Connection con) {
        return this.getFrom().equals(con.getFrom()) && this.getTo().equals(con.getTo()) && this.offset == con.offset;
    }

    public BlockPos getMin() {
        return this.compared < 0 ? this.to : this.from;
    }

    public BlockPos getMax() {
        return this.compared >= 0 ? this.to : this.from;
    }

    public boolean brokenSide(World world, boolean next) {
        TileEntity te = world.getTileEntity(next == this.compared < 0 ? this.next : this.previous);
        if(te instanceof ConnectableBlockEntity) {
            ConnectableBlockEntity fe = (ConnectableBlockEntity) te;
            for (Connection fenceConnection : fe.getConnections()) {
                if(this.lazyEquals(fenceConnection) && fenceConnection.isBroken()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean isPowered(IBlockAccess world) {
        for (BlockPos pos : LineUtils.getBlocksInbetween(this.from, this.to, this.offset)) { //TODO: cache ?
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof ConnectableBlockEntity) {
                boolean has = false;
                for (Connection connection : ((ConnectableBlockEntity) te).getConnections()) {
                    if(connection.lazyEquals(this)) {
                        if(connection.isBroken()) {
                            return false;
                        }
                        has = true;
                        break;
                    }
                }
                if(!has) {
                    return false;
                }
            } else {
                return false;
            }
        }

        for(BlockPos pos : Lists.newArrayList(this.from, this.to)) {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof BlockEntityElectricFencePole && te.hasCapability(CapabilityEnergy.ENERGY, null) && Objects.requireNonNull(te.getCapability(CapabilityEnergy.ENERGY, null)).getEnergyStored() > 0) {
                return true;
            }
        }

        return false;
    }

    public VboCache getCache() {
        return this.rendercache = this.getOrGenCache(this.rendercache);
    }

    public void invalidateCache() {
        this.rendercache = null;
    }

    private VboCache getOrGenCache(VboCache cache) {
        if(cache != null) {
            return cache;
        }
        double halfthick = this.type.getCableWidth() / 2F;

        double posdist = this.distance(this.from, this.to.getX()+0.5F, this.to.getZ()+0.5F);
        double yrange = (this.to.getY() - from.getY()) / posdist;
        double tangrad = this.in[1] == this.in[0] ? Math.PI/2D : Math.atan((this.in[2] - this.in[3]) / (this.in[1] - this.in[0]));
        double xcomp = halfthick * Math.sin(tangrad);
        double zcomp = halfthick * Math.cos(tangrad);
        double tangrady = posdist == 0 ? Math.PI/2D : Math.atan((this.to.getY() - this.from.getY()) / posdist);
        double yxzcomp = Math.sin(tangrady) ;
        double[] ct = new double[] {
                this.in[0] - xcomp + yxzcomp*zcomp, this.in[2] - zcomp - yxzcomp*xcomp,
                this.in[1] - xcomp + yxzcomp*zcomp, this.in[3] - zcomp - yxzcomp*xcomp,
                this.in[1] + xcomp + yxzcomp*zcomp, this.in[3] + zcomp - yxzcomp*xcomp,
                this.in[0] + xcomp + yxzcomp*zcomp, this.in[2] + zcomp - yxzcomp*xcomp
        };
        double[] cb = new double[] {
                this.in[0] - xcomp - yxzcomp*zcomp, this.in[2] - zcomp + yxzcomp*xcomp,
                this.in[1] - xcomp - yxzcomp*zcomp, this.in[3] - zcomp + yxzcomp*xcomp,
                this.in[1] + xcomp - yxzcomp*zcomp, this.in[3] + zcomp + yxzcomp*xcomp,
                this.in[0] + xcomp - yxzcomp*zcomp, this.in[2] + zcomp + yxzcomp*xcomp
        };
        double[] cent = new double[] {
                (ct[0] + ct[2])/2D,
                (ct[1] + ct[3])/2D,
                (ct[4] + ct[6])/2D,
                (ct[5] + ct[7])/2D
        };
        double[] cenb = new double[] {
                (cb[0] + cb[2])/2D,
                (cb[1] + cb[3])/2D,
                (cb[4] + cb[6])/2D,
                (cb[5] + cb[7])/2D
        };
        double ytop = yrange * this.distance(this.from, this.in[0], this.in[2]) - this.position.getY() + this.from.getY();
        double ybot = yrange * this.distance(this.from, this.in[1], this.in[3]) - this.position.getY() + this.from.getY();
        double len = Math.sqrt(Math.pow(ct[0] == ct[2] ? ct[1]-ct[3] : ct[0]-ct[2], 2) + (ytop-ybot)*(ytop-ybot)) / (halfthick*32F);
        double yThick = halfthick * Math.cos(tangrady);
        Pair<int[], int[]> prev = this.genRenderCache(false, new double[]{ct[0], ct[1], cent[0], cent[1], cent[2], cent[3], ct[6], ct[7]}, new double[] {cb[0], cb[1], cenb[0], cenb[1], cenb[2], cenb[3], cb[6], cb[7]}, yThick, len, ytop, ybot);
        Pair<int[], int[]> next = this.genRenderCache(true,  new double[]{cent[0], cent[1], ct[2], ct[3], ct[4], ct[5], cent[2], cent[3]}, new double[] {cenb[0], cenb[1], cb[2], cb[3], cb[4], cb[5], cenb[2], cenb[3]}, yThick, len, ytop, ybot);
        double[] uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        int[] data = new int[144];
        RenderUtils.buildSpacedCube(new ConnectionRenderer(data, -this.position.getX(), (float) this.offset, -this.position.getZ()),
                ct[0], ytop + yThick, ct[1],
                ct[2], ybot + yThick, ct[3],
                ct[4], ybot + yThick, ct[5],
                ct[6], ytop + yThick, ct[7],
                cb[0], ytop - yThick, cb[1],
                cb[2], ybot - yThick, cb[3],
                cb[4], ybot - yThick, cb[5],
                cb[6], ytop - yThick, cb[7],
                uvs[0], uvs[1],
                uvs[2], uvs[3],
                uvs[4], uvs[5],
                uvs[6], uvs[7],
                uvs[8], uvs[9],
                uvs[10], uvs[11],
                len,this.type.getCableWidth(),this.type.getCableWidth()
        );
        return new VboCache(data, prev.getLeft(), next.getLeft(), prev.getRight(), next.getRight());
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    private Pair<int[], int[]> genRenderCache(boolean next, double[] ct, double[] cb, double yThick, double len, double ytop, double ybot) {
        Vector3d point = next ? this.nextCache.point : this.prevCache.point;
        double ycenter = ybot + (ytop - ybot) / 2D;
        int[] rotated = new int[144];
        double[] uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        if(next) {
            ytop = ycenter;
            RenderUtils.buildSpacedCube(new ConnectionRenderer(rotated, -this.position.getX(), (float) this.offset, -this.position.getZ()),
                    ct[0] + point.x, ycenter + yThick + point.y, ct[1] + point.z,
                    ct[0], ycenter + yThick, ct[1],
                    ct[6], ycenter + yThick, ct[7],
                    ct[6] + point.x, ycenter + yThick + point.y, ct[7] + point.z,

                    cb[0] + point.x, ycenter - yThick + point.y, cb[1] + point.z,
                    cb[0], ycenter - yThick, cb[1],
                    cb[6], ycenter - yThick, cb[7],
                    cb[6] + point.x, ycenter - yThick + point.y, cb[7] + point.z,

                    uvs[0], uvs[1],
                    uvs[2], uvs[3],
                    uvs[4], uvs[5],
                    uvs[6], uvs[7],
                    uvs[8], uvs[9],
                    uvs[10], uvs[11],
                    len/2, this.type.getCableWidth(), this.type.getCableWidth()
            );
        } else {
            ybot = ycenter;
            RenderUtils.buildSpacedCube(new ConnectionRenderer(rotated, -this.position.getX(), (float) this.offset, -this.position.getZ()),
                    ct[2], ycenter + yThick, ct[3],
                    ct[2] - point.x, ycenter + yThick - point.y, ct[3] - point.z,
                    ct[4] - point.x, ycenter + yThick - point.y, ct[5] - point.z,
                    ct[4], ycenter + yThick, ct[5],

                    cb[2], ycenter - yThick, cb[3],
                    cb[2] - point.x, ycenter - yThick - point.y, cb[3] - point.z,
                    cb[4] - point.x, ycenter - yThick - point.y, cb[5] - point.z,
                    cb[4], ycenter - yThick, cb[5],

                    uvs[0], uvs[1],
                    uvs[2], uvs[3],
                    uvs[4], uvs[5],
                    uvs[6], uvs[7],
                    uvs[8], uvs[9],
                    uvs[10], uvs[11],
                    len/2, this.type.getCableWidth(), this.type.getCableWidth()
            );
        }

        uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        int[] fixed = new int[144];
        RenderUtils.buildSpacedCube(new ConnectionRenderer(fixed, -this.position.getX(), (float) this.offset, -this.position.getZ()),
                ct[0], ytop + yThick, ct[1],
                ct[2], ybot + yThick, ct[3],
                ct[4], ybot + yThick, ct[5],
                ct[6], ytop + yThick, ct[7],
                cb[0], ytop - yThick, cb[1],
                cb[2], ybot - yThick, cb[3],
                cb[4], ybot - yThick, cb[5],
                cb[6], ytop - yThick, cb[7],
                uvs[0], uvs[1],
                uvs[2], uvs[3],
                uvs[4], uvs[5],
                uvs[6], uvs[7],
                uvs[8], uvs[9],
                uvs[10], uvs[11],
                len/2,this.type.getCableWidth(),this.type.getCableWidth()
        );
        return Pair.of(fixed, rotated);
    }

    @Value public class SurroundingCache {Vector3d point; RotatedRayBox fixedBox, rotatedBox; }

    @Value public class VboCache { int[] data, prevFixed, nextFixed, prevRotated, nextRotated; }

    private final static class ConnectionRenderer implements RenderUtils.FaceRenderer {

        private final int[] data;
        private final float x;
        private final float y;
        private final float z;
        private int o;

        private ConnectionRenderer(int[] data, float x, float y, float z) {
            this.data = data;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public RenderUtils.FaceRenderer pos(double x, double y, double z) {
            this.data[this.o] = Float.floatToRawIntBits((float)x + this.x);
            this.data[this.o + 1] = Float.floatToRawIntBits((float)y + this.y);
            this.data[this.o + 2] = Float.floatToRawIntBits((float)z + this.z);
            return this;
        }

        @Override
        public RenderUtils.FaceRenderer tex(double u, double v) {
            this.data[this.o + 3] = Float.floatToRawIntBits((float)u);
            this.data[this.o + 4] = Float.floatToRawIntBits((float)v);
            return this;
        }

        @Override
        public RenderUtils.FaceRenderer normal(float x, float y, float z) {
            int xn = (byte)((int)(x * 127.0F)) & 255;
            int yn = (byte)((int)(y * 127.0F)) & 255;
            int zn = (byte)((int)(z * 127.0F)) & 255;
            this.data[this.o + 5] = xn | yn << 8 | zn << 16;
            return this;
        }

        @Override
        public RenderUtils.FaceRenderer endVertex() {
            this.o += 6;
            return this;
        }
    }
}
