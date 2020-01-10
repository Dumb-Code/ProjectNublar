package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.function.Consumer;

@Value
@EqualsAndHashCode(callSuper = true)
public class MetabolismInformation extends TrackingDataInformation {

    public static final String KEY = "metabolism_info";

    private final int food;
    private final int maxFood;

    private final int water;
    private final int maxWater;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    public void addTooltip(Consumer<String> lineAdder) {
        lineAdder.accept(I18n.format("projectnublar.gui.tracking.metatlism.food", this.food, this.maxFood));
        lineAdder.accept(I18n.format("projectnublar.gui.tracking.metatlism.water", this.water, this.maxWater));
        super.addTooltip(lineAdder);
    }

    public static void encodeNBT(NBTTagCompound nbt, MetabolismInformation info) {
        nbt.setInteger("food", info.food);
        nbt.setInteger("max_food", info.maxFood);

        nbt.setInteger("water", info.water);
        nbt.setInteger("max_water", info.maxWater);
    }

    public static MetabolismInformation decodeNBT(NBTTagCompound nbt) {
        return new MetabolismInformation(
            nbt.getInteger("food"), nbt.getInteger("max_food"),
            nbt.getInteger("water"), nbt.getInteger("max_water")
        );
    }

    public static void encodeBuf(ByteBuf buf, MetabolismInformation info) {
        buf.writeInt(info.food);
        buf.writeInt(info.maxFood);
        buf.writeInt(info.water);
        buf.writeInt(info.maxWater);
    }

    public static MetabolismInformation decodeBuf(ByteBuf buf) {
        return new MetabolismInformation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
}
