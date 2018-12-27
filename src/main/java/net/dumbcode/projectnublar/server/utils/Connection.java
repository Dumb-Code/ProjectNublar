package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;
import lombok.Value;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Vector3f;

@Getter
public class Connection {
    private final ConnectionType type;
    private final BlockPos from;
    private final BlockPos to;

    private final BlockPos previous;
    private final BlockPos next;

    private final BlockPos position;

    private final boolean hasSign;

    private final int compared;

    private Cache[] rendercache;

    public Connection(ConnectionType type, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position, boolean hasSign) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.previous = previous;
        this.next = next;
        this.position = position;
        this.hasSign = hasSign;

        this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();

        this.rendercache = new Cache[this.type.getOffsets().length];
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", this.type.ordinal());
        nbt.setLong("from", this.getFrom().toLong());
        nbt.setLong("to", this.getTo().toLong());
        nbt.setBoolean("sign", this.hasSign);
        return nbt;
    }

    public static Connection fromNBT(NBTTagCompound nbt, TileEntity tileEntity) {
        return new Connection(ConnectionType.getType(nbt.getInteger("type")), BlockPos.fromLong(nbt.getLong("from")), BlockPos.fromLong(nbt.getLong("to")), BlockPos.fromLong(nbt.getLong("previous")), BlockPos.fromLong(nbt.getLong("next")), tileEntity.getPos(), nbt.getBoolean("sign"));
    }


    public BlockPos getMin() {
        return this.compared < 0 ? this.to : this.from;
    }

    public BlockPos getMax() {
        return this.compared >= 0 ? this.to : this.from;
    }

    public Cache getCache(int index) {
        return this.rendercache[index] = this.getOrGenCache(this.rendercache[index], index);
    }

    private Cache getOrGenCache(Cache cache, int index) {
        if(cache != null) {
            return cache;
        }
        double halfthick = this.type.getCableWidth() / 2F;

        BlockPos to = this.getMax();
        BlockPos from = this.getMin();

        double posdist = this.distance(from, to.getX()+0.5F, to.getZ()+0.5F);
        double yrange = (to.getY() - from.getY()) / posdist;
        double[] in = LineUtils.intersect(this.position, from, to, this.type.getOffsets()[index]);
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
            double ytop = yrange * this.distance(from, in[0], in[2]) - this.position.getY() + from.getY();
            double ybot = yrange * this.distance(from, in[1], in[3]) - this.position.getY() + from.getY();
            double len = Math.sqrt(Math.pow(ct[0] == ct[2] ? ct[1]-ct[3] : ct[0]-ct[2], 2) + (ytop-ybot)*(ytop-ybot));
            double yThick = halfthick *  Math.cos(tangrady);
            Vector3f xNorm = MathUtils.calcualeNormalF(
                    ct[2], ybot+yThick, ct[3],
                    cb[2], ybot-yThick, cb[3],
                    cb[4], ybot-yThick, cb[5]
            );
            Vector3f zNorm = MathUtils.calcualeNormalF( //outwards
                    ct[0], ytop+yThick, ct[1],
                    cb[0], ytop-yThick, cb[1],
                    cb[2], ybot-yThick, cb[3]
            );
            return new Cache(ct, cb, in, xNorm, zNorm, ybot, ytop, len, Math.sqrt((in[1]-in[0])*(in[1]-in[0]) + (in[3]-in[2])*(in[3]-in[2])), yThick, halfthick*2);
        }
        return null;
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    @Value public class Cache { double[] ct, cb, in; Vector3f xNorm, zNorm; double ybot, ytop, len, xzlen, yThick, fullThick; }
}
