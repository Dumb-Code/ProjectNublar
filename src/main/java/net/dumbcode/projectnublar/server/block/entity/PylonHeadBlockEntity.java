package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class PylonHeadBlockEntity extends SimpleBlockEntity {

    public static final double WEIGHT_PER_BLOCK = 0.1D;

    private final Set<Connection> connections = new HashSet<>();
    private UUID networkUUID = UUID.randomUUID();

    public PylonHeadBlockEntity() {
        super(ProjectNublarBlockEntities.PYLON_HEAD.get());
    }


    public void addConnection(Connection connection) {
        this.connections.add(connection);
        this.syncToClient();
        this.setChanged();
    }

    public Set<BlockPos> gatherAllNetworkLocations(Set<BlockPos> set) {
        if(!set.contains(this.worldPosition)) {
            set.add(this.worldPosition);
            for (Connection connection : this.connections) {
                BlockPos other = connection.getOther(this.worldPosition);
                TileEntity entity = this.level.getBlockEntity(other);
                if(entity instanceof PylonHeadBlockEntity) {
                    ((PylonHeadBlockEntity) entity).gatherAllNetworkLocations(set);
                }
            }
        }
        return set;
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        compound.put("Connections", this.connections.stream().collect(CollectorUtils.functionMapper(CompoundNBT::new, (connection, tag) -> {
            tag.put("FromPos", NBTUtil.writeBlockPos(connection.getFrom()));
            tag.put("ToPos", NBTUtil.writeBlockPos(connection.getTo()));
        })).collect(CollectorUtils.toNBTTagList()));
        compound.putUUID("NetworkID", this.networkUUID);
        return super.save(compound);
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        this.connections.clear();
        StreamUtils.stream(compound.getList("Connections", Constants.NBT.TAG_COMPOUND))
            .map(t -> (CompoundNBT) t)
            .map(t -> new Connection(NBTUtil.readBlockPos(t.getCompound("FromPos")), NBTUtil.readBlockPos(t.getCompound("ToPos"))))
            .forEach(this.connections::add);
        this.networkUUID = compound.getUUID("NetworkID");
        super.load(state, compound);
    }

    @Value
    public static class Connection {
        BlockPos from;
        BlockPos to;

        double a, b, c;

        public Connection(BlockPos from, BlockPos to) {
            this.from = from;
            this.to = to;

            double dist = Math.sqrt(from.distSqr(to));

            double s = from.getY();                                            //beizer start
            double m = (from.getY() + to.getY()) / 2D - WEIGHT_PER_BLOCK*dist; //beizer middle
            double e = to.getY();                                              //beizer end


            //The definition of a 3 point bezier curve is as follows:
            //(1-t)^2s + 2(1-t)mt + et^2
            //Where s, m and e are 3 different points on the curve.
            //For optimization and for the differentiation to work, I want to get the equation in the form of at^2 + bt + c
            // = (1-2t+t^2)s + (2t-2t^2)m + et^2
            // = s - 2st + st^2 + 2mt - 2mt^2 + et^2
            // = (s - 2m + e)t^2 + (-2s + 2m)t + (s)
            // = at^2 + bt + c, where:
            //                      a = s - 2m + e
            //                      b = -2s + 2m
            //                      c = s
            this.a = s - 2*m + e;
            this.b = -2*s + 2*m;
            this.c = s;
        }

        public BlockPos getOther(BlockPos pos) {
            return this.from.equals(pos) ? this.to : this.from;
        }

        public double beizerCurve(double t) {
            return this.a*t*t + this.b*t + this.c;
        }

        public double beizerCurveGradient(double t) {
            return 2*this.a*t + this.b;
        }

    }
}
