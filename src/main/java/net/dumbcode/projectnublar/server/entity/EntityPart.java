package net.dumbcode.projectnublar.server.entity;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityPart extends Entity implements IEntityAdditionalSpawnData {

    private int parentID = -1; //todo sync parent
    private String partName;

    private boolean setInParent = false;

    public EntityPart(@Nonnull Entity parent, @Nonnull String partName) {
        super(parent.world);
        this.parentID = parent.getEntityId();
        this.partName = partName;
        this.setInParent = true;
    }

    public EntityPart(World world) {
        super(world);
    }


    @Nullable
    public Entity getParent() {
        return this.world.getEntityByID(this.parentID);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void onUpdate() {
        if(!this.setInParent) {
            Entity parent = this.getParent();
            this.setInParent |= parent != null;
            if(parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(EntityComponentTypes.MULTIPART)
                        .ifPresent(multipartEntityComponent -> multipartEntityComponent.entities.add(new MultipartEntityComponent.LinkedEntity(this.partName, this)));
            }
        }
        super.onUpdate();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity parent = this.getParent();
        return parent != null ? parent.attackEntityFrom(source, amount) : super.attackEntityFrom(source, amount);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.parentID != -1);
        if(this.parentID != -1) {
            buffer.writeInt(this.parentID);
            ByteBufUtils.writeUTF8String(buffer, this.partName);
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if(additionalData.readBoolean()) {
            this.parentID = additionalData.readInt();
            this.partName = ByteBufUtils.readUTF8String(additionalData);
        }
    }
}
