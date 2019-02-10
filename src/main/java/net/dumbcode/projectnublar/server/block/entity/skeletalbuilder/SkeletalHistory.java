package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.utils.HistoryList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SkeletalHistory {

    public static final String RESET_NAME = "$$RESET_NAME$$";

    @Getter private final HistoryList<Record> history = new HistoryList<>();
    @Getter private Map<String, Vector3f> editingData = new HashMap<>();

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Index", this.history.getIndex());
        NBTTagList list = new NBTTagList();
        for (Record record : this.history.getUnindexedList()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("Part", record.getPart());
            tag.setFloat("AngleX", record.getAngle().x);
            tag.setFloat("AngleY", record.getAngle().y);
            tag.setFloat("AngleZ", record.getAngle().z);
            list.appendTag(tag);
        }
        nbt.setTag("Records", list);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.history.clear();
        for (NBTBase record : nbt.getTagList("Records", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound tag = (NBTTagCompound) record;
            this.history.add(new Record(tag.getString("Part"), new Vector3f(tag.getFloat("AngleX"), tag.getFloat("AngleY"), tag.getFloat("AngleZ"))));
        }
        this.history.setIndex(nbt.getInteger("Index"));
    }

    public void clear() {
        this.history.clear();
    }

    public void redo() {
        this.history.redo();
    }

    public void undo() {
        this.history.undo();
    }

    public void add(Record record) {
        this.editingData.remove(record.getPart());
        this.history.add(record);
    }

    @Value public static class Record { private String part; private Vector3f angle; }
}
