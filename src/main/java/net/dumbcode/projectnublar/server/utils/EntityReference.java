package net.dumbcode.projectnublar.server.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityReference<E extends Entity> {
    protected final Class<E> entityClass;
    protected final Predicate<E> entityPredicate;

    @Nullable
    protected E reference;
    @Nullable
    protected UUID entityUUID;

    public EntityReference(Class<E> entityClass) {
        this(entityClass, e -> true);
    }

    public EntityReference(Class<E> entityClass, Predicate<E> entityPredicate) {
        this.entityClass = entityClass;
        this.entityPredicate = entityPredicate;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public E get(World world) {
        if(this.reference == null && this.entityUUID != null) {
            for (Entity entity : world.loadedEntityList) {
                if(entity.getUniqueID().equals(this.entityUUID) && this.entityClass.isInstance(entity) && this.entityPredicate.test((E) entity)) {
                    this.entityUUID = entity.getUniqueID();
                    return this.reference = (E) entity;
                }
            }
            this.entityUUID = null;
        }
        return this.reference;
    }

    @Nullable
    public E getRawReference() {
        return this.reference;
    }

    public void reset() {
        this.reference = null;
        this.entityUUID = null;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        if(this.entityUUID != null) {
            nbt.setBoolean("has", true);
            nbt.setUniqueId("uuid", this.entityUUID);
        }
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        if(nbt.getBoolean("has")) {
            this.entityUUID = nbt.getUniqueId("uuid");
        }
    }

    public void writeToByteBuf(ByteBuf buf) {
        buf.writeBoolean(this.entityUUID != null);
        if(this.entityUUID != null) {
            buf.writeLong(this.entityUUID.getLeastSignificantBits());
            buf.writeLong(this.entityUUID.getMostSignificantBits());
        }
    }

    public void readFromByteBuf(ByteBuf buf) {
        if(buf.readBoolean()) {
            this.entityUUID = new UUID(buf.readLong(), buf.readLong());
        }
    }
}
