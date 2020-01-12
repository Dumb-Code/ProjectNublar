package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Value
@EqualsAndHashCode(callSuper = true)
public class BasicEntityInformation extends TooltipInformation {

    public static final String KEY = "basic_entity_information";

    private final float health;
    private final float maxHealth;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    protected List<String> getTooltipLines() {
        return Collections.singletonList(I18n.format("projectnublar.gui.tracking.health", this.health, this.maxHealth));
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
