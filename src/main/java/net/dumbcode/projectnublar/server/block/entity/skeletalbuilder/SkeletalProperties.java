package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import com.google.common.collect.Lists;
import lombok.Data;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

import java.util.List;

@Data
public class SkeletalProperties {

    private float rotation = 0;
    private PoleFacing poleFacing = PoleFacing.NONE;
    private final List<String> poleList = Lists.newArrayList();

    public NBTTagCompound serialize(NBTTagCompound nbt) {
        nbt.setFloat("Rotation", this.rotation);
        nbt.setString("FacingDirection", this.poleFacing.name());
        NBTTagList list = new NBTTagList();
        for (String pole : this.poleList) {
            list.appendTag(new NBTTagString(pole));
        }
        nbt.setTag("PoleList", list);
        return nbt;
    }

    public void deserialize(NBTTagCompound nbt) {
        this.rotation = nbt.getFloat("Rotation");
        this.poleFacing = PoleFacing.valueOf(nbt.getString("FacingDirection"));
        this.poleList.clear();
        for (NBTBase list : nbt.getTagList("PoleList", Constants.NBT.TAG_STRING)) {
            this.poleList.add(((NBTTagString)list).getString());
        }
    }
}
