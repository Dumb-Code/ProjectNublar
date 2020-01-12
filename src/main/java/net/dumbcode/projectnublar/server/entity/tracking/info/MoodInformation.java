package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.Value;
import net.dumbcode.dumblibrary.server.utils.IOCollectors;
import net.dumbcode.dumblibrary.server.utils.StreamUtils;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class MoodInformation extends TooltipInformation {

    public static final String KEY = "mood_info";


    private final int mood;
    private final List<MoodReason> reasons;

    private final List<MoodReason> positiveReasons;
    private final List<MoodReason> negativeReasons;

    public MoodInformation(List<MoodReason> reasons) {
        this.reasons = Collections.unmodifiableList(reasons);
        this.mood = reasons.stream().mapToInt(MoodReason::getChange).sum();

        Map<Boolean, List<MoodReason>> collected = reasons.stream().sorted(Comparator.comparing(r -> Math.abs(r.getChange()))).collect(Collectors.partitioningBy(r -> r.change > 0));
        this.positiveReasons = collected.get(true);
        this.negativeReasons = collected.get(false);
    }

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    protected List<String> getTooltipLines() {
        List<String> lines = new ArrayList<>();

        int toShow = 3;

        lines.add("Reasons Happy:");
        this.positiveReasons.stream().limit(toShow).forEachOrdered(a -> lines.add("+" + a.change + ":" + a.reason));

        lines.add("Reasons Angry:");
        this.negativeReasons.stream().limit(toShow).forEachOrdered(a -> lines.add(a.change + ":" + a.reason));

        return lines;
    }


    public static void encodeNBT(NBTTagCompound nbt, MoodInformation info) {
        nbt.setTag("reasons", info.reasons.stream().map(MoodReason::serialize).collect(IOCollectors.toNBTTagList()));
    }

    public static MoodInformation decodeNBT(NBTTagCompound nbt) {
        return new MoodInformation(
            StreamUtils.stream(nbt.getTagList("reasons", Constants.NBT.TAG_COMPOUND))
                .map(b -> MoodReason.deserialize((NBTTagCompound) b))
                .collect(Collectors.toList())
        );
    }

    public static void encodeBuf(ByteBuf buf, MoodInformation info) {
        buf.writeShort(info.reasons.size());
        info.reasons.forEach(r -> MoodReason.serialize(r, buf));
    }

    public static MoodInformation decodeBuf(ByteBuf buf) {
        return new MoodInformation(IntStream.range(0, buf.readShort()).mapToObj(i -> MoodReason.deserialize(buf)).collect(Collectors.toList()));
    }

    @Value
    public static class MoodReason {
        private final int change;
        private final String reason;

        public static NBTTagCompound serialize(MoodReason reason) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("change", reason.change);
            compound.setString("reason", reason.reason);
            return compound;
        }

        public static MoodReason deserialize(NBTTagCompound compound) {
            return new MoodReason(compound.getInteger("change"), compound.getString("reason"));
        }

        public static void serialize(MoodReason reason, ByteBuf buf) {
            buf.writeInt(reason.change);
            ByteBufUtils.writeUTF8String(buf, reason.reason);
        }

        public static MoodReason deserialize(ByteBuf buf) {
            return new MoodReason(buf.readInt(), ByteBufUtils.readUTF8String(buf));
        }
    }
}
