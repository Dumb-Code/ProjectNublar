package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.List;

@Data
public class SkeletalProperties {

    private float rotation = 0;
    private float prevRotation = 0;
    private List<Pole> poles = Lists.newArrayList();

    public CompoundNBT serialize(CompoundNBT nbt) {
        nbt.putFloat("Rotation", this.rotation);
        ListNBT list = new ListNBT();
        for (Pole pole : this.poles) {
            CompoundNBT tag = new CompoundNBT();
            tag.putString("CubeName", pole.cubeName);
            tag.putInt("Facing", pole.facing.ordinal());
            list.add(tag);
        }
        nbt.put("PoleList", list);
        return nbt;
    }

    public void deserialize(CompoundNBT nbt) {
        this.rotation = nbt.getFloat("Rotation");
        this.poles.clear();
        for (INBT list : nbt.getList("PoleList", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT tag = (CompoundNBT) list;
            this.poles.add(new Pole(tag.getString("CubeName"), PoleFacing.values()[tag.getInt("Facing")]));
        }
    }

    @Data
    @AllArgsConstructor
    public static class Pole {
        private String cubeName;
        private PoleFacing facing;
    }
}
