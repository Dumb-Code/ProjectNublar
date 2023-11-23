package net.dumbcode.projectnublar.server.entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.vector.Vector3d;

public class DataSerializerHandler {
    public final static IDataSerializer<Vector3d> VEC_3D = new IDataSerializer<Vector3d>() {

        @Override
        public void write(FriendlyByteBuf buf, Vector3d value) {
            buf.writeFloat((float) value.x);
            buf.writeFloat((float) value.y);
            buf.writeFloat((float) value.z);
        }

        @Override
        public Vector3d read(FriendlyByteBuf buf) {
            return new Vector3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
        }

        @Override
        public Vector3d copy(Vector3d value) {
            return value;
        }
    };

    public static void register() {

        DataSerializers.registerSerializer(VEC_3D);

    }
}
