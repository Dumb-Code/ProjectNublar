package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Value;
import net.dumbcode.dumblibrary.server.SimpleBlockEntity;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

@Getter
public class PylonHeadBlockEntity extends SimpleBlockEntity {

    public static final double WEIGHT_PER_BLOCK = 0.1D;

    private final Set<Connection> connections = new HashSet<>();

    public void addConnection(Connection connection) {
        this.connections.add(connection);
        this.syncToClient();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Connections", this.connections.stream().collect(CollectorUtils.functionMapper(NBTTagCompound::new, (connection, tag) -> {
            tag.setLong("FromPos", connection.getFrom().toLong());
            tag.setLong("ToPos", connection.getTo().toLong());
        })).collect(CollectorUtils.toNBTTagList()));
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.connections.clear();
        StreamUtils.stream(compound.getTagList("Connections", Constants.NBT.TAG_COMPOUND))
            .map(NBTTagCompound.class::cast)
            .map(t -> new Connection(BlockPos.fromLong(t.getLong("FromPos")), BlockPos.fromLong(t.getLong("ToPos"))))
            .forEach(this.connections::add);
        super.readFromNBT(compound);
    }

    @Value
    public static class Connection {
        BlockPos from;
        BlockPos to;

        double a, b, c;

        public Connection(BlockPos from, BlockPos to) {
            this.from = from;
            this.to = to;

            double dist = Math.sqrt(from.distanceSq(to));

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

        public double beizerCurve(double t) {
            return this.a*t*t + this.b*t + this.c;
        }

        public double beizerCurveGradient(double t) {
            return 2*this.a*t + this.b;
        }

    }
}
