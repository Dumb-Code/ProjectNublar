package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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

        lines.add(I18n.format("projectnublar.gui.tracking.mood", I18n.format(this.mood)));
        this.reasons.forEach(s -> lines.add(" - " + I18n.format(s)));
        return lines;
    }


    public static void encodeNBT(NBTTagCompound nbt, MoodInformation info) {
        nbt.setString("mood", info.mood);
        nbt.setTag("reasons", info.reasons.stream().collect(IOCollectors.toNBTList(NBTTagString::new)));
    }

    public static MoodInformation decodeNBT(NBTTagCompound nbt) {
        return new MoodInformation(
            nbt.getString("mood"),
            StreamUtils.stream(nbt.getTagList("reasons", Constants.NBT.TAG_STRING))
                .map(b -> ((NBTTagString) b).getString())
                .collect(Collectors.toList())
        );
    }

    public static void encodeBuf(ByteBuf buf, MoodInformation info) {
        ByteBufUtils.writeUTF8String(buf, info.mood);
        buf.writeShort(info.reasons.size());
        info.reasons.forEach(r -> ByteBufUtils.writeUTF8String(buf, r));
    }

    public static MoodInformation decodeBuf(ByteBuf buf) {
        return new MoodInformation(ByteBufUtils.readUTF8String(buf), IntStream.range(0, buf.readShort()).mapToObj(i -> ByteBufUtils.readUTF8String(buf)).collect(Collectors.toList()));
    }
}
