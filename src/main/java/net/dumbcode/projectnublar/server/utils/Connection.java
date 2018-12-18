package net.dumbcode.projectnublar.server.utils;

import lombok.Getter;
import lombok.Value;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Vector3f;

@Getter
public class Connection {
    private final BlockPos from;
    private final BlockPos to;
    private final BlockPos position;

    private final int compared;

    private Cache renderCacheLow;
    private Cache renderCacheHigh;

    public Connection(BlockPos from, BlockPos to, BlockPos position) {
        this.from = from;
        this.to = to;
        this.position = position;

        this.compared = this.from.getX() == this.to.getX() ? this.to.getZ() - this.from.getZ() : this.from.getX() - this.to.getX();
    }


    public BlockPos getMin() {
        return this.compared < 0 ? this.to : this.from;
    }

    public BlockPos getMax() {
        return this.compared >= 0 ? this.to : this.from;
    }

    public Cache getRenderCacheLow() {
        return this.renderCacheLow = this.getOrGenCache(this.renderCacheLow, 0.25F);
    }

    public Cache getRenderCacheHigh() {
        return this.renderCacheHigh = this.getOrGenCache(this.renderCacheHigh, 0.75F);
    }

    private Cache getOrGenCache(Cache cache, double off) {
        if(cache != null) {
            return cache;
        }
        double halfthick = 1/32F;

        BlockPos to = this.getMax();
        BlockPos from = this.getMin();

        double posdist = this.distance(from, to.getX()+0.5F, to.getZ()+0.5F);
        double yrange = (to.getY() - from.getY()) / posdist;
        double[] in = LineUtils.intersect(this.position, from, to, off);
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
            return new Cache(ct, cb, xNorm, zNorm, ybot, ytop, len, yThick, halfthick*2);
        }
        return null;
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    @Value public class Cache { double[] ct, cb; Vector3f xNorm, zNorm; double ybot, ytop, len, yThick, fullThick; }
}
