package net.dumbcode.projectnublar.server.entity.tracking.info;

import lombok.Data;
import net.dumbcode.dumblibrary.server.utils.CollectorUtils;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class MoodInformation extends TooltipInformation {

    public static final String KEY = "mood_info";


    private final String mood;
    private final List<String> reasons;

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    protected List<String> getTooltipLines() {
        List<String> lines = new ArrayList<>();

        lines.add(I18n.get("projectnublar.gui.tracking.mood", I18n.get(this.mood)));
        this.reasons.forEach(s -> lines.add(" - " + I18n.get(s)));
        return lines;
    }


    public static void encodeNBT(CompoundNBT nbt, MoodInformation info) {
        nbt.putString("mood", info.mood);
        nbt.put("reasons", info.reasons.stream().collect(CollectorUtils.toNBTList(StringNBT::valueOf)));
    }

    public static MoodInformation decodeNBT(CompoundNBT nbt) {
        return new MoodInformation(
            nbt.getString("mood"),
            StreamUtils.stream(nbt.getList("reasons", Constants.NBT.TAG_STRING))
                .map(INBT::toString)
                .collect(Collectors.toList())
        );
    }

    public static void encodeBuf(PacketBuffer buf, MoodInformation info) {
        buf.writeUtf(info.mood);
        buf.writeShort(info.reasons.size());
        info.reasons.forEach(buf::writeUtf);
    }

    public static MoodInformation decodeBuf(PacketBuffer buf) {
        return new MoodInformation(
            buf.readUtf(),
            IntStream.range(0, buf.readShort()).mapToObj(i -> buf.readUtf()).collect(Collectors.toList())
        );
    }
}
