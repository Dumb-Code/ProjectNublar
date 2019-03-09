package net.dumbcode.projectnublar.server.utils;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Iterator;

public class ItemStackUtils {
    public static boolean compareControlledNbt(@Nullable NBTBase controlled, @Nullable NBTBase tested) {
        if(controlled == null) {
            return true;
        }
        if(tested == null) {
            return false;
        }
        if(controlled.getId() != tested.getId()) {
            return false;
        }

        switch (controlled.getId()) {
            case Constants.NBT.TAG_END: return true;
            case Constants.NBT.TAG_LIST:
                NBTTagList controlledList = (NBTTagList) controlled;
                NBTTagList testedList = (NBTTagList) tested;
                if(controlledList.tagCount() != testedList.tagCount() || controlledList.getTagType() != testedList.getTagType()) {
                    return false;
                }
                Iterator<NBTBase> citer = controlledList.iterator();
                Iterator<NBTBase> titer = testedList.iterator();
                while (citer.hasNext() && titer.hasNext()) {
                    if(!compareControlledNbt(citer.next(), titer.next())) {
                        return false;
                    }
                }
                return true;
            case Constants.NBT.TAG_COMPOUND:
                NBTTagCompound controlledTag = (NBTTagCompound) controlled;
                NBTTagCompound testedTag = (NBTTagCompound) tested;
                for (String key : controlledTag.getKeySet()) {
                    if(!compareControlledNbt(controlledTag.getTag(key), testedTag.getTag(key))) {
                        return false;
                    }
                }
                return true;
            default: return controlled.equals(tested);
        }
    }
}
