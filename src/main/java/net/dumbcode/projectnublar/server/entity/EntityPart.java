package net.dumbcode.projectnublar.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.MultipartEntityComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityPart extends Entity implements IEntityAdditionalSpawnData {

    private UUID parentUUID;
    @Getter private String partName;

    public double cubeWidth;
    public double cubeHeight;
    public double cubeDepth;

    private Entity parentCache;

    private boolean setInParent = false;

    public EntityPart(@Nonnull Entity parent, @Nonnull String partName) {
        this(parent.world);
        this.entityCollisionReduction = 0.5F;
        this.parentUUID = parent.getUniqueID();
        this.partName = partName;
    }

    public EntityPart(World world) {
        super(world);
    }


    @Nullable
    public Entity getParent() {
        if(this.parentCache != null) {
            return parentCache;
        }
        for (Entity entity : this.world.loadedEntityList) {
            if(entity.getUniqueID().equals(this.parentUUID)) {
                this.parentCache = entity;
            }
        }
        return this.parentCache;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.parentUUID = compound.getUniqueId("parent");
        this.partName = compound.getString("partname");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setUniqueId("parent", this.parentUUID);
        compound.setString("partname", this.partName);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void onUpdate() {
        Entity parent = this.getParent();
        if((this.setInParent && parent == null) || (parent != null && parent.isDead)) {
            this.setDead();
        }

        if(!this.setInParent) {
            this.setInParent = parent != null;
            if(parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(EntityComponentTypes.MULTIPART)
                        .ifPresent(multipartEntityComponent -> multipartEntityComponent.entities.add(new MultipartEntityComponent.LinkedEntity(this.partName, this.getUniqueID())));
            }
        }

        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(this));
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if(!(entity instanceof EntityPart)) {
                    this.applyEntityCollision(entity);
                }
            }
        }
        super.onUpdate();
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        double width = this.cubeWidth / 2.0F;
        double depth = this.cubeDepth / 2.0F;

        this.height = (float) this.cubeHeight;
        this.setEntityBoundingBox(new AxisAlignedBB(x - width, y, z - depth, x + width, y + this.cubeHeight, z + depth));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        Entity parent = this.getParent();
        return parent != null ? parent.attackEntityFrom(source, amount) : super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if(entityIn == this.getParent()) {
            return;
        }
        super.applyEntityCollision(entityIn);
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        Entity p = this.getParent();
        if(p != null) {
            p.addVelocity(x, y, z);
        } else {
            super.addVelocity(x, y, z);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.parentUUID != null);
        if(this.parentUUID != null) {
            buffer.writeLong(this.parentUUID.getMostSignificantBits());
            buffer.writeLong(this.parentUUID.getLeastSignificantBits());
            ByteBufUtils.writeUTF8String(buffer, this.partName);
        }
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if(additionalData.readBoolean()) {
            this.parentUUID = new UUID(additionalData.readLong(), additionalData.readLong());
            this.partName = ByteBufUtils.readUTF8String(additionalData);
        }
    }
}
