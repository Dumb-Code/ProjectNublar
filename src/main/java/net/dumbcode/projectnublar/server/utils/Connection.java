package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.*;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import java.util.Objects;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static lombok.EqualsAndHashCode.Include;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Connection {
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

    private Cache rendercache;
    @Setter private boolean broken;

    public Connection(ConnectionType type, double offset, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this.type = type;
        this.offset = offset;
        this.from = from;
        this.to = to;
        this.previous = previous;
        this.next = next;
        this.position = position;

        this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();

        this.toFromHash = (this.compared < 0 ? this.from : this.to).hashCode() + (this.compared < 0 ? this.to : this.from).hashCode() * 31;

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
        Connection c = new Connection(ConnectionType.getType(nbt.getInteger("type")), nbt.getDouble("offset"), BlockPos.fromLong(nbt.getLong("from")), BlockPos.fromLong(nbt.getLong("to")), BlockPos.fromLong(nbt.getLong("previous")), BlockPos.fromLong(nbt.getLong("next")), tileEntity.getPos());
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

    public boolean brokenSide(World world, BlockPos otherPos) {
        TileEntity te = world.getTileEntity(otherPos);
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

    public Cache getCache() {
        return this.rendercache = this.getOrGenCache(this.rendercache);
    }

    public void invalidateCache() {
        this.rendercache = null;
    }

    private Cache getOrGenCache(Cache cache) {
        if(cache != null) {
            return cache;
        }
        double halfthick = this.type.getCableWidth() / 2F;

        BlockPos to = this.getMax();
        BlockPos from = this.getMin();

        double posdist = this.distance(from, to.getX()+0.5F, to.getZ()+0.5F);
        double yrange = (to.getY() - from.getY()) / posdist;
        double[] in = LineUtils.intersect(this.position, from, to, this.offset);
        Random rand = new Random(this.getPosition().toLong() + (long)(this.getOffset() * 1000));
        if(in != null) {
            double tangrad = in[1] == in[0] ? Math.PI/2D : Math.atan((in[2] - in[3]) / (in[1] - in[0]));
            double xcomp = halfthick * Math.sin(tangrad);
            double zcomp = halfthick * Math.cos(tangrad);
            double tangrady = posdist == 0 ? Math.PI/2D : Math.atan((to.getY() - from.getY()) / posdist);
            double yxzcomp = Math.sin(tangrady) ;
            double[] ct = new double[] {
                    in[0] - xcomp + yxzcomp*zcomp, in[2] - zcomp - yxzcomp*xcomp,
                    in[1] - xcomp + yxzcomp*zcomp, in[3] - zcomp - yxzcomp*xcomp,
                    in[1] + xcomp + yxzcomp*zcomp, in[3] + zcomp - yxzcomp*xcomp,
                    in[0] + xcomp + yxzcomp*zcomp, in[2] + zcomp - yxzcomp*xcomp
            };
            double[] cb = new double[] {
                    in[0] - xcomp - yxzcomp*zcomp, in[2] - zcomp + yxzcomp*xcomp,
                    in[1] - xcomp - yxzcomp*zcomp, in[3] - zcomp + yxzcomp*xcomp,
                    in[1] + xcomp - yxzcomp*zcomp, in[3] + zcomp + yxzcomp*xcomp,
                    in[0] + xcomp - yxzcomp*zcomp, in[2] + zcomp + yxzcomp*xcomp
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



            double ytop = yrange * this.distance(from, in[0], in[2]) - this.position.getY() + from.getY();
            double ybot = yrange * this.distance(from, in[1], in[3]) - this.position.getY() + from.getY();
            double len = Math.sqrt(Math.pow(ct[0] == ct[2] ? ct[1]-ct[3] : ct[0]-ct[2], 2) + (ytop-ybot)*(ytop-ybot)) / (halfthick*32F);
            double yThick = halfthick * Math.cos(tangrady);
            double sqxz = (in[1]-in[0])*(in[1]-in[0]) + (in[3]-in[2])*(in[3]-in[2]);

            double fullLen = Math.sqrt(sqxz + (in[5]-in[4])*(in[5]-in[4]));

            BreakCache prevB = this.genCache(in, new double[]{ct[0], ct[1], cent[0], cent[1], cent[2], cent[3], ct[6], ct[7]}, new double[] {cb[0], cb[1], cenb[0], cenb[1], cenb[2], cenb[3], cb[6], cb[7]}, fullLen, Math.sqrt(sqxz), halfthick, rand);
            BreakCache nextB = this.genCache(in, new double[]{cent[0], cent[1], ct[2], ct[3], ct[4], ct[5], cent[2], cent[3]}, new double[] {cenb[0], cenb[1], cb[2], cb[3], cb[4], cb[5], cenb[2], cenb[3]}, fullLen, Math.sqrt(sqxz), halfthick, rand);

            RotatedRayBox box = new RotatedRayBox.Builder(new AxisAlignedBB(0, -halfthick * 2, -halfthick * 2, -fullLen, halfthick * 2, halfthick * 2))
                    .origin(in[0], in[4], in[2])
                    .rotate(Math.atan((in[5] - in[4]) / Math.sqrt(sqxz)), 0, 0, 1)
                    .rotate(in[1] == in[0] ? Math.PI*1.5D : Math.atan((in[3] - in[2]) / (in[1] - in[0])), 0, 1, 0)
                    .build();

            return new Cache(ct, cb, in, IntStream.range(0, 24).mapToDouble(i -> rand.nextInt(16)/16F).toArray(), new Vector3d((in[0]+in[1])/2, (in[4]+in[5])/2, (in[2]+in[3])/2), prevB, nextB, ybot, ytop, len, Math.sqrt(sqxz), fullLen, yThick, halfthick*2, box);
        }
//        throw new IllegalStateException("Error generating cache, Connection did not was not intersected: " + this);
        return null;
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    private BreakCache genCache(double[] in, double[] ct, double[] cb, double fullLen, double xzlen, double halfthick, Random rand) {
        Vector3d point = new Vector3d(fullLen/2, 0, 0);
        RotatedRayBox box = new RotatedRayBox.Builder(new AxisAlignedBB(0, -halfthick * 2, -halfthick * 2, -fullLen/2, halfthick * 2, halfthick * 2))
                .rotate(Math.atan((in[5] - in[4]) / xzlen), 0, 0, 1)
                .rotate(in[1] == in[0] ? Math.PI*1.5D : Math.atan((in[3] - in[2]) / (in[1] - in[0])), 0, 1, 0)

                .rotate((rand.nextFloat()-0.5F) * Math.PI/2F, 0, 0, 1)
                .rotate((rand.nextFloat()-0.5F) * Math.PI/2F, 0, 1, 0)
                .build();
        box.getBackwards().transform(point);
        return new BreakCache(ct, cb, IntStream.range(0, 24).mapToDouble(i -> rand.nextInt(16)/16F).toArray(), point, box);
    }

    @Value public class Cache { double[] ct, cb, in, uvs; Vector3d center; BreakCache prev, next; double ybot, ytop, texLen, XZlen, fullLen, yThick, fullThick; RotatedRayBox rayBox; }

    @Value public class BreakCache { double[] ct, cb, uvs; Vector3d point; RotatedRayBox rayBox;}
}
