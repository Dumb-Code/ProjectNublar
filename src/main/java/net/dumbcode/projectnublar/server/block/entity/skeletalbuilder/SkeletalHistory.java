package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.utils.HistoryList;
import net.dumbcode.projectnublar.server.utils.RotatedRayBox;
import net.dumbcode.projectnublar.server.utils.RotationAxis;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.vecmath.Vector3f;
import java.util.*;

public class SkeletalHistory {

    public static final String RESET_NAME = "$$RESET_NAME$$";

    @Getter private final HistoryList<List<Record>> history = new HistoryList<>();
    @Getter private Map<String, Edit> editingData = new HashMap<>();

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("Index", this.history.getIndex());
        NBTTagList list = new NBTTagList();
        for (List<Record> records : this.history.getUnindexedList()) {
            NBTTagList taglist = new NBTTagList();
            for (Record record : records) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Part", record.getPart());
                tag.setFloat("AngleX", record.getAngle().x);
                tag.setFloat("AngleY", record.getAngle().y);
                tag.setFloat("AngleZ", record.getAngle().z);
                taglist.appendTag(tag);
            }
            list.appendTag(taglist);
        }
        nbt.setTag("Records", list);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.history.clear();
        for (NBTBase record : nbt.getTagList("Records", Constants.NBT.TAG_LIST)) {
            List<Record> records = Lists.newArrayList();
            for (NBTBase nbtBase : (NBTTagList) record) {
                NBTTagCompound tag = (NBTTagCompound) nbtBase;
                records.add(new Record(tag.getString("Part"), new Vector3f(tag.getFloat("AngleX"), tag.getFloat("AngleY"), tag.getFloat("AngleZ"))));
            }
            this.history.add(records);
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
        this.history.add(Lists.newArrayList(record));
    }

    public void addGroupedRecord(List<Record> records) {
        this.history.add(records);
    }

    @Value public static class Record { String part; Vector3f angle; }

    @Value public static class Edit { RotationAxis axis; float angle; }
}
