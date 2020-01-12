package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Value
public class PregnancyInformation extends TooltipInformation {

    public static final String KEY = "pregnancy_information";

    private int[] ticksTillGiveBirth;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    protected List<String> getTooltipLines() {
        List<String> lines = new ArrayList<>();
        if(this.ticksTillGiveBirth.length != 0) {
            Map<Integer, Integer> countMap = new HashMap<>();

            for (int i : this.ticksTillGiveBirth) {
                countMap.compute(i, (key, value) -> {
                    if(value == null) {
                        return 1;
                    }
                    return value + 1;
                });
            }

            List<Integer> keys = new ArrayList<>(countMap.keySet());
            Collections.sort(keys);

            lines.add(I18n.format("projectnublar.gui.tracking.pregnancy"));

            for (Integer key : keys) {
                int hours = key / 72000;
                int minutes = (key % 72000) / 1200;
                int seconds = (key % 1200) / 20;
                lines.add(I18n.format("projectnublar.gui.tracking.pregnancy.entry", countMap.get(key), hours, minutes, seconds));
            }
        }
        return lines;
    }

    public static void encodeNBT(NBTTagCompound nbt, PregnancyInformation info) {
        nbt.setIntArray("ticks", info.ticksTillGiveBirth);
    }

    public static PregnancyInformation decodeNBT(NBTTagCompound nbt) {
        return new PregnancyInformation(nbt.getIntArray("ticks"));
    }

    public static void encodeBuf(ByteBuf buf, PregnancyInformation info) {
        buf.writeShort(info.ticksTillGiveBirth.length);
        for (int i : info.ticksTillGiveBirth) {
            buf.writeInt(i);
        }
    }

    public static PregnancyInformation decodeBuf(ByteBuf buf) {
        return new PregnancyInformation(IntStream.range(0, buf.readShort()).map(i -> buf.readInt()).toArray());
    }
}
