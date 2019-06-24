package net.dumbcode.projectnublar.server.entity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.Vec3d;

public class DataSerializerHandler {
    public final static DataSerializer<Vec3d> VEC_3D = new DataSerializer<Vec3d>() {
        @Override
        public void write(PacketBuffer buf, Vec3d value) {
            buf.writeFloat((float) value.x);
            buf.writeFloat((float) value.y);
            buf.writeFloat((float) value.z);
        }

        @Override
        public Vec3d read(PacketBuffer buf) {
            return new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public DataParameter<Vec3d> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public Vec3d copyValue(Vec3d value) {
            return value;
        }
    };

    public static void register() {

        DataSerializers.registerSerializer(VEC_3D);

    }
}
