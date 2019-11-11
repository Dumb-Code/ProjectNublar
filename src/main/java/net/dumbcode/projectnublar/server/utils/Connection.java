package net.dumbcode.projectnublar.server.utils;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.dumbcode.projectnublar.server.block.entity.ConnectableBlockEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import static lombok.EqualsAndHashCode.Include;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Accessors(chain = true)
public class Connection {
    @Include private final ConnectionType type;
    @Include private final double offset;
    //Used to help compare Connections
    @Include private final int toFromHash;
    private final BlockPos from;
    private final BlockPos to;

    private final BlockPos next;
    private final BlockPos previous;

    @Setter boolean sign;

    private final BlockPos position;
    private final int compared;

    private VboCache rendercache;
    @Setter private boolean broken;

    private final double[] in;
    private final boolean valid;

    private final double xzlen;
    private final double fullLen;

    private final Random random;

    private final Vector3d center;
    private final RotatedRayBox rayBox;

    private final SurroundingCache prevCache;
    private final SurroundingCache nextCache;

    public Connection(ConnectionType type, double offset, BlockPos from, BlockPos to, BlockPos previous, BlockPos next, BlockPos position) {
        this.type = type;
        this.offset = offset;
        this.position = position;

        if((this.compared = (from.getX() == to.getX() ? to.getZ() - from.getZ() : from.getX() - to.getX())) < 0) {
            BlockPos ref = from;
            from = to;
            to = ref;

            ref = previous;
            previous = next;
            next = ref;

        }

        this.from = from;
        this.to = to;
        this.next = next;
        this.previous = previous;

        this.toFromHash = (this.compared < 0 ? this.from : this.to).hashCode() + (this.compared < 0 ? this.to : this.from).hashCode() * 31;

        double[] intercept = LineUtils.intersect(this.position, this.from, this.to, this.offset);
        if(intercept == null) {
           intercept = new double[6]; //ew
            this.valid = false;
        } else {
            this.valid = true;
        }

        this.random = new Random(this.getPosition().toLong() * (long)(this.getOffset() * 1000));
        this.in = intercept;
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
        Vector3d point = new Vector3d((next ? 1 : -1) * this.fullLen/2, 0, 0);
        double w = this.type.getCableWidth();
        RotatedRayBox fixedBox = new RotatedRayBox.Builder(new AxisAlignedBB(0, -w, -w, -this.fullLen/2, w, w))
                .origin(next?this.center.x:this.in[0], next?this.center.y:this.in[4], next?this.center.z:this.in[2])
                .rotate(Math.atan((this.in[5] - this.in[4]) / this.xzlen), 0, 0, 1)
                .rotate(this.in[1] == this.in[0] ? Math.PI*1.5D : Math.atan((this.in[3] - this.in[2]) / (this.in[1] - this.in[0])), 0, 1, 0)
                .build();
        double yang = (this.random.nextFloat()-0.5F) * Math.PI/3F;
        double zang = (this.random.nextFloat()-0.5F) * Math.PI/3F;
        RotatedRayBox rotatedBox = this.genRotatedBox((next ? 1 : -1) * this.fullLen/2, yang, zang);
        rotatedBox.getBackwards().transform(point);
        AxisAlignedBB aabb = new AxisAlignedBB(this.position);
        Vec3d centerVec = new Vec3d(this.center.x, this.center.y, this.center.z);
        Vec3d vec3d = new Vec3d(point.x, point.y, point.z).add(centerVec);
        if(!aabb.contains(vec3d)) { //Point outside of bounding box. Cant happen for selction box reasons
            RayTraceResult result = aabb.calculateIntercept(centerVec, vec3d);
            if(result != null && result.hitVec != null) {
                double dist = result.hitVec.distanceTo(centerVec) * (next ? 1 : -1) ;
                rotatedBox = this.genRotatedBox(dist, yang, zang);
                point = new Vector3d(dist, 0, 0);
                rotatedBox.getBackwards().transform(point);
            }
        }
        return new SurroundingCache(point, fixedBox, rotatedBox);
    }

    public Connection copy() {
        return new Connection(this.type, this.offset, this.from, this.to, this.previous, this.next, this.position).setBroken(this.broken);
    }
    private RotatedRayBox genRotatedBox(double length, double yang, double zang) {
        double w = this.type.getCableWidth();
        return new RotatedRayBox.Builder(new AxisAlignedBB(0, -w, -w, length, w, w))
                .origin(this.center.x, this.center.y, this.center.z)
                .rotate((this.in[1] == this.in[0] ? Math.PI*1.5D : Math.atan((this.in[3] - this.in[2]) / (this.in[1] - this.in[0]))) + yang, 0, 1, 0)
                .rotate(Math.atan((this.in[5] - this.in[4]) / this.xzlen) + zang, 0, 0, 1)
                .build();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("id", this.type.getRegistryName().toString());
        nbt.setDouble("offset", this.offset);
        nbt.setLong("from", this.getFrom().toLong());
        nbt.setLong("to", this.getTo().toLong());
        nbt.setBoolean("sign", this.sign);
        nbt.setLong("next", this.next.toLong());
        nbt.setLong("previous", this.previous.toLong());
        nbt.setBoolean("broken", this.broken);
        return nbt;
    }


    public static Connection fromNBT(NBTTagCompound nbt, TileEntity tileEntity) {
        return new Connection(ConnectionType.getType(new ResourceLocation(nbt.getString("id"))), nbt.getDouble("offset"), BlockPos.fromLong(nbt.getLong("from")), BlockPos.fromLong(nbt.getLong("to")), BlockPos.fromLong(nbt.getLong("previous")), BlockPos.fromLong(nbt.getLong("next")), tileEntity.getPos()).setBroken(nbt.getBoolean("broken")).setSign(nbt.getBoolean("sign"));
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
        TileEntity te = world.getTileEntity(next == this.compared < 0 ? this.previous : this.next);
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
        for (BlockPos pos : LineUtils.getBlocksInbetween(this.from, this.to, this.offset)) {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof ConnectableBlockEntity) {
                if(!this.isContactablePowerAllowed((ConnectableBlockEntity) te)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        for(BlockPos pos : Lists.newArrayList(this.from, this.to)) {
            TileEntity te = world.getTileEntity(pos);
            if(te != null && te.hasCapability(CapabilityEnergy.ENERGY, null) && Objects.requireNonNull(te.getCapability(CapabilityEnergy.ENERGY, null)).getEnergyStored() > 0) {
                return true;
            }
        }

        return false;
    }
    
    private boolean isContactablePowerAllowed(ConnectableBlockEntity connectable) {
        boolean has = false;
        for (Connection connection : connectable.getConnections()) {
            if(connection.lazyEquals(this)) {
                if(connection.isBroken()) {
                    return false;
                }
                has = true;
                break;
            }
        }
        return has;
    }

    public VboCache getCache() {
        return this.rendercache = this.getOrGenCache(this.rendercache);
    }

    private VboCache getOrGenCache(VboCache cache) {
        if(cache != null) {
            return cache;
        }
        double halfthick = this.type.getCableWidth() / 2F;

        double posdist = this.distance(this.from, this.to.getX()+0.5F, this.to.getZ()+0.5F);
        double yrange = posdist == 0 ? 1 : (this.to.getY() - this.from.getY()) / posdist;
        double tangrad = this.in[1] == this.in[0] ? Math.PI/2D : Math.atan((this.in[2] - this.in[3]) / (this.in[1] - this.in[0]));
        double xcomp = halfthick * Math.sin(tangrad);
        double zcomp = halfthick * Math.cos(tangrad);
        double tangrady = posdist == 0 ? Math.PI/2D : Math.atan((this.to.getY() - this.from.getY()) / posdist);
        double yxzcomp = Math.sin(tangrady);
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
        Tessellator.getInstance().getBuffer().setTranslation(-this.position.getX(), this.offset, -this.position.getZ());
        Pair<Integer, Integer> prevRenderCache = this.genRenderCache(false, new double[]{ct[0], ct[1], cent[0], cent[1], cent[2], cent[3], ct[6], ct[7]}, new double[] {cb[0], cb[1], cenb[0], cenb[1], cenb[2], cenb[3], cb[6], cb[7]}, yThick, len, ytop, ybot);
        Pair<Integer, Integer> nextRenderCache = this.genRenderCache(true,  new double[]{cent[0], cent[1], ct[2], ct[3], ct[4], ct[5], cent[2], cent[3]}, new double[] {cenb[0], cenb[1], cb[2], cb[3], cb[4], cb[5], cenb[2], cenb[3]}, yThick, len, ytop, ybot);
        double[] uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        int main = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(main, GL11.GL_COMPILE);
        RenderUtils.drawSpacedCube(
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
        GlStateManager.glEndList();
        Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
        return new VboCache(main, prevRenderCache.getLeft(), nextRenderCache.getLeft(), prevRenderCache.getRight(), nextRenderCache.getRight());
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }

    private Pair<Integer, Integer> genRenderCache(boolean next, double[] ct, double[] cb, double yThick, double len, double ytop, double ybot) {
        Vector3d point = next ? this.nextCache.point : this.prevCache.point;
        double ycenter = ybot + (ytop - ybot) / 2D;
        double[] uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        int rotated = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(rotated, GL11.GL_COMPILE);
        if(next) {
            ytop = ycenter;
            RenderUtils.drawSpacedCube(
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
            RenderUtils.drawSpacedCube(
                    ct[2], ycenter + yThick, ct[3],
                    ct[2] + point.x, ycenter + yThick + point.y, ct[3] + point.z,
                    ct[4] + point.x, ycenter + yThick + point.y, ct[5] + point.z,
                    ct[4], ycenter + yThick, ct[5],

                    cb[2], ycenter - yThick, cb[3],
                    cb[2] + point.x, ycenter - yThick + point.y, cb[3] + point.z,
                    cb[4] + point.x, ycenter - yThick + point.y, cb[5] + point.z,
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
        GlStateManager.glEndList();
        uvs = IntStream.range(0, 12).mapToDouble(i -> this.random.nextInt(16)/16F).toArray();
        int fixed = GlStateManager.glGenLists(1);
        GlStateManager.glNewList(fixed, GL11.GL_COMPILE);
        RenderUtils.drawSpacedCube(
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
        GlStateManager.glEndList();
        return Pair.of(fixed, rotated);
    }

    @Value public class SurroundingCache {Vector3d point; RotatedRayBox fixedBox, rotatedBox; }

    @Value public class VboCache { int data, prevFixed, nextFixed, prevRotated, nextRotated; }
}
