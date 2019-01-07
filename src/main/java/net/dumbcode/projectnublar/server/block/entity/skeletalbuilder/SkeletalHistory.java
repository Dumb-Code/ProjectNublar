package net.dumbcode.projectnublar.server.block.entity.skeletalbuilder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.network.S1UpdateSkeletalBuilder;
import net.dumbcode.projectnublar.server.network.S5UpdateHistoryIndex;
import net.dumbcode.projectnublar.server.network.S7FullPoseChange;
import net.minecraft.client.model.ModelRenderer;
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

    public static final String RESET_NAME = "@@RESET_NAME@@";
    private final SkeletalBuilderBlockEntity builder;
    @Getter
    @Setter
    private int index;
    @Getter
    private final List<Record> records = new LinkedList<>();
    private final Vector3f preparationAngles = new Vector3f();

    public SkeletalHistory(SkeletalBuilderBlockEntity builder) {
        this.builder = builder;
    }

    public int getHistoryLength() {
        return records.size();
    }

    public void truncateAtIndex() {
        while(records.size() > index+1) {
            records.remove(records.size()-1);
        }
    }

    public boolean atHistoryBeginning() {
        return index == 0;
    }

    public boolean atHistoryEnd() {
        return index >= getHistoryLength()-1;
    }

    public void record(String partThatMoved) {
        ModelRenderer part = getPart(partThatMoved);
        if(part == null && !partThatMoved.equals(RESET_NAME))
            return;
        if(!atHistoryEnd()) {
            truncateAtIndex();
        }
        Record record = new Record();
        record.setPart(partThatMoved);
        if(partThatMoved.equals(RESET_NAME)) {
            Map<String, Vector3f> copy = deepCopy(builder.getPoseData());
            record.setPreviousPoseData(copy);
        } else {
            record.setPreviousAngles(new Vector3f(preparationAngles));
            record.setNewAngles(copyAngles(part));
        }
        records.add(record);
        if(records.size() > 1)
            index++;
    }

    private Map<String, Vector3f> deepCopy(Map<String, Vector3f> original) {
        Map<String, Vector3f> copy = new HashMap<>();
        for(Map.Entry<String, Vector3f> entry : original.entrySet()) {
            copy.put(entry.getKey(), new Vector3f(entry.getValue()));
        }
        return copy;
    }

    private Vector3f copyAngles(ModelRenderer part) {
        return new Vector3f(part.rotateAngleX, part.rotateAngleY, part.rotateAngleZ);
    }

    private ModelRenderer getPart(String name) {
        if(builder.getModel() == null)
            return null;
        for(ModelRenderer box : builder.getModel().boxList) {
            if(box.boxName.equals(name))
                return box;
        }
        return null;
    }

    public void prepareForRecording(String partName) {
        ModelRenderer part = getPart(partName);
        if(part != null) {
            preparationAngles.x = part.rotateAngleX;
            preparationAngles.y = part.rotateAngleY;
            preparationAngles.z = part.rotateAngleZ;
        } else {
            preparationAngles.x = 0f;
            preparationAngles.y = 0f;
            preparationAngles.z = 0f;
        }
    }

    public boolean moveIndex(int direction) {
        int newIndex = index+direction;
        if(newIndex < 0)
            return false;
        if(newIndex >= getHistoryLength())
            return false;
        Vector3f anglesToApply;
        String affectedPart;
        if(direction > 0) {
            anglesToApply = records.get(newIndex).newAngles;
            affectedPart = records.get(newIndex).part;
        } else {
            anglesToApply = records.get(index).previousAngles;
            affectedPart = records.get(index).part;
        }
        if(affectedPart.equals(RESET_NAME)) {
            if(direction > 0) {
                builder.resetPoseDataToDefaultPose();
            } else {
                builder.getPoseData().clear();
                builder.getPoseData().putAll(deepCopy(records.get(index).previousPoseData));
            }
            ProjectNublar.NETWORK.sendToAll(new S7FullPoseChange(builder, builder.getPoseData()));
        } else {
            builder.updateAngles(affectedPart, anglesToApply);
            ProjectNublar.NETWORK.sendToAll(new S1UpdateSkeletalBuilder(builder, affectedPart, anglesToApply));
        }
        ProjectNublar.NETWORK.sendToAll(new S5UpdateHistoryIndex(builder, newIndex));
        this.index = newIndex;
        builder.markDirty();
        return true;
    }

    public void recordPoseReset() {
        record(RESET_NAME);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("index", getIndex());
        NBTTagList recordList = new NBTTagList();
        for(Record record : records) {
            NBTTagCompound recordNBT = new NBTTagCompound();
            recordNBT.setString("partID", record.part);
            if(record.isResetRecord()) {
                recordNBT.setTag("poseData", builder.writePoseToNBT(record.previousPoseData));
            } else {
                recordNBT.setFloat("prevX", record.previousAngles.x);
                recordNBT.setFloat("prevY", record.previousAngles.y);
                recordNBT.setFloat("prevZ", record.previousAngles.z);
                recordNBT.setFloat("newX", record.newAngles.x);
                recordNBT.setFloat("newY", record.newAngles.y);
                recordNBT.setFloat("newZ", record.newAngles.z);
            }
            recordList.appendTag(recordNBT);
        }
        nbt.setTag("records", recordList);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        index = nbt.getInteger("index");
        records.clear();
        NBTTagList recordsNBT = nbt.getTagList("records", Constants.NBT.TAG_COMPOUND);
        for(NBTBase nbtBase : recordsNBT) {
            NBTTagCompound tag = (NBTTagCompound)nbtBase;
            Record record = new Record();
            record.setPart(tag.getString("partID"));
            if(record.isResetRecord()) {
                record.setPreviousPoseData(builder.readPoseFromNBT(tag.getCompoundTag("poseData")));
            } else {
                record.setNewAngles(new Vector3f(tag.getFloat("newX"), tag.getFloat("newY"), tag.getFloat("newZ")));
                record.setPreviousAngles(new Vector3f(tag.getFloat("prevX"), tag.getFloat("prevY"), tag.getFloat("prevZ")));
            }
            records.add(record);
        }
    }

    @Data
    private class Record {
        private String part;
        private Vector3f previousAngles;
        private Vector3f newAngles;
        private Map<String, Vector3f> previousPoseData;

        public boolean isResetRecord() {
            return part.equals(RESET_NAME);
        }
    }

    public enum MovementType {
        STARTING, STOPPING
    }
}
