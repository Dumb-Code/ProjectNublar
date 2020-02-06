package net.dumbcode.projectnublar.server.entity.tracking.info;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

@RequiredArgsConstructor
public class SleepInformation extends TooltipInformation {

    public static final String KEY = "string_info";

    private final double tiredness;
    private final double chance;

    @Override
    protected List<String> getTooltipLines() {
        return Lists.newArrayList(
            "Tiredness: " + this.tiredness,
            "Chance of sleeping (min): " + this.chance
        );
    }

    @Override
    protected String getTypeName() {
        return KEY;
    }

    public static void encodeNBT(NBTTagCompound nbt, SleepInformation info) {
        nbt.setDouble("tiredness", info.tiredness);
        nbt.setDouble("chance", info.chance);
    }

    public static SleepInformation decodeNBT(NBTTagCompound nbt) {
        return new SleepInformation(nbt.getDouble("tiredness"), nbt.getDouble("chance"));
    }

    public static void encodeBuf(ByteBuf buf, SleepInformation info) {
        buf.writeDouble(info.tiredness);
        buf.writeDouble(info.chance);
    }

    public static SleepInformation decodeBuf(ByteBuf buf) {
        return new SleepInformation(buf.readDouble(), buf.readDouble());
    }
}
