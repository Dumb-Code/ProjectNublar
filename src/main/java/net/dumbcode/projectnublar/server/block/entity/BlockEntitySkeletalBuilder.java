package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraftforge.items.ItemStackHandler;

public class BlockEntitySkeletalBuilder extends SimpleBlockEntity {
    private final ItemStackHandler boneHandler = new ItemStackHandler();
    private Dinosaur dinosaur = Dinosaur.MISSING;
    private Rotation rotation = Rotation.NONE;

    public BlockEntitySkeletalBuilder() {
        this.reassureSize();
    }

    @Getter(lazy = true)
    private final DinosaurEntity dinosaurEntity = createEntity();

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("Dinsoaur", this.dinosaur.getRegName().toString());
        nbt.setTag("Inventory", this.boneHandler.serializeNBT());
        nbt.setInteger("Rotation", this.rotation.ordinal());
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.dinosaur = ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(nbt.getString("Dinosaur")));
        this.boneHandler.deserializeNBT(nbt.getCompoundTag("Inventory"));
        this.rotation = Rotation.values()[nbt.getInteger("Rotation")];
        this.reassureSize();
        super.readFromNBT(nbt);
    }

    private void reassureSize() {
        int size = this.dinosaur.getSkeletalInfomation().getBoneListed().size();
        if(size != this.boneHandler.getSlots()) {
            this.boneHandler.setSize(size); //TODO: Maybe make a diffrent method that keeps the items?
        }
    }

    public Dinosaur getDinosaur() {
        return dinosaur;
    }

    public void setDinosaur(Dinosaur dinosaur) {
        this.dinosaur = dinosaur;
        this.reassureSize();
    }

    public ItemStackHandler getBoneHandler() {
        return boneHandler;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    private DinosaurEntity createEntity() {
        DinosaurEntity entity = this.dinosaur.createEntity(this.world);
        //Initilize stuff
        return entity;
    }
}
