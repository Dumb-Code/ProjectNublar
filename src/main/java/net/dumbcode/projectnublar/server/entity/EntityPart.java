package net.dumbcode.projectnublar.server.entity;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class EntityPart extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Vector3d> WATCHER_SIZE = EntityDataManager.defineId(EntityPart.class, DataSerializerHandler.VEC_3D);

    private UUID parentUUID;
    @Getter private String partName;

    private Entity parentCache;

    private boolean setInParent = false;

    public EntityPart(@Nonnull Entity parent, @Nonnull String partName) {
        this(EntityHandler.DUMMY_PART.get(), parent.level);
//        this.entityCollisionReduction = 0.5F;
        this.parentUUID = parent.getUUID();
        this.partName = partName;
    }

    public EntityPart(EntityType<?> type, World world) {
        super(type, world);
    }

    public void setSize(Vector3d size) {
        this.entityData.set(WATCHER_SIZE, size);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(WATCHER_SIZE, Vector3d.ZERO);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        Entity parent = this.getParent();
        if(!this.level.isClientSide && ((this.setInParent && parent == null) || (parent != null && !parent.isAlive()))) {
            this.kill();
        }

        if(!this.setInParent) {
            this.setInParent = parent != null;
            if(parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(ComponentHandler.MULTIPART)
                        .ifPresent(multipartEntityComponent -> multipartEntityComponent.getEntities().add(new MultipartEntityComponent.LinkedEntity(this.partName, this.getUUID())));

            }
        }
        super.tick();
    }

    @Override
    public boolean canCollideWith(Entity part) {
        return !(part instanceof EntityPart);
    }

    @Nullable
    public Entity getParent() {
        if (this.parentCache != null) {
            return parentCache;
        }
        if(this.level instanceof ServerWorld) {
            this.parentCache = ((ServerWorld) this.level).getEntity(this.parentUUID);
        } else {
            for (Entity entity : ((ClientWorld) this.level).entitiesForRendering()) {
                if (entity.getUUID().equals(this.parentUUID)) {
                    this.parentCache = entity;
                }
            }
        }
        return this.parentCache;
    }


    @Override
    public void setPos(double x, double y, double z) {
        this.setPosRaw(x, y, z);
        Vector3d size = this.entityData == null ? Vector3d.ZERO : this.entityData.get(WATCHER_SIZE);
        double width = size.x / 2.0F;
        double height = size.y;
        double depth = size.z / 2.0F;

        this.setBoundingBox(new AxisAlignedBB(x - width, y, z - depth, x + width, y + height, z + depth));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity parent = this.getParent();
        return parent != null ? parent.hurt(source, amount) : super.hurt(source, amount);
    }

    @Override
    public void push(Entity entity) {
        if(entity == this.getParent()) {
            return;
        }
        super.push(entity);
    }

//    @Override
//    public void addVelocity(double x, double y, double z) {
//        Entity p = this.getParent();
//        if(p != null) {
//            p.addVelocity(x, y, z);
//        } else {
//            super.addVelocity(x, y, z);
//        }
//    }

//    @Override
//    public boolean canBePushed() {
//        return true;
//    }

    @Override
    public boolean isPushable() {
        return true;
    }


    @Override
    protected void readAdditionalSaveData(CompoundNBT compound) {
        this.parentUUID = compound.getUUID("parent");
        this.partName = compound.getString("partname");
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT compound) {
        compound.putUUID("parent", this.parentUUID);
        compound.putString("partname", this.partName);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(this.parentUUID != null);
        if(this.parentUUID != null) {
            buffer.writeLong(this.parentUUID.getMostSignificantBits());
            buffer.writeLong(this.parentUUID.getLeastSignificantBits());
            buffer.writeUtf(this.partName);
        }
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        if(additionalData.readBoolean()) {
            this.parentUUID = new UUID(additionalData.readLong(), additionalData.readLong());
            this.partName = additionalData.readUtf();
        }
    }
}
