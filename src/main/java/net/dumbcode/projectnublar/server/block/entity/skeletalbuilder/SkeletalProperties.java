package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@Data
public class SkeletalProperties {

    private float rotation = 0;
    @SideOnly(Side.CLIENT) private float prevRotation = 0;
    private List<Pole> poles = Lists.newArrayList();

    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setFloat("Rotation", this.rotation);
        NBTTagList list = new NBTTagList();
        for (Pole pole : this.poles) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("CubeName", pole.cubeName);
            tag.setInteger("Facing", pole.facing.ordinal());
            list.appendTag(tag);
        }
        nbt.setTag("PoleList", list);
        return nbt;
    }

    public void deserialize(NBTTagCompound nbt) {
        this.rotation = nbt.getFloat("Rotation");
        this.poles.clear();
        for (NBTBase list : nbt.getTagList("PoleList", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound tag = (NBTTagCompound) list;
            this.poles.add(new Pole(tag.getString("CubeName"), PoleFacing.values()[tag.getInteger("Facing")]));
        }
    }

    @Data
    @AllArgsConstructor
    public static class Pole {
        private String cubeName;
        private PoleFacing facing;
    }
}
