package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

@Value
@EqualsAndHashCode(callSuper = true)
public class BasicEntityInformation extends TrackingDataInformation {

    public static final String KEY = "basic_entity_information";

    private final float health;
    private final float maxHealth;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    public void addTooltip(Consumer<String> lineAdder) {
        lineAdder.accept("Health: " + this.health + "/" + this.maxHealth);
        super.addTooltip(lineAdder);
    }

    public static void encodeNBT(NBTTagCompound nbt, BasicEntityInformation info) {
        nbt.setFloat("health", info.health);
        nbt.setFloat("max_health", info.maxHealth);
    }

    public static BasicEntityInformation decodeNBT(NBTTagCompound nbt) {
        return new BasicEntityInformation(
            nbt.getFloat("health"),
            nbt.getFloat("max_health")
        );
    }

    public static void encodeBuf(ByteBuf buf, BasicEntityInformation info) {
        buf.writeFloat(info.health);
        buf.writeFloat(info.maxHealth);
    }

    public static BasicEntityInformation decodeBuf(ByteBuf buf) {
        return new BasicEntityInformation(
            buf.readFloat(),
            buf.readFloat()
        );
    }
}
