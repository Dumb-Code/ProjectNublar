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

public class EntityPart extends Entity implements IEntityAdditionalSpawnData {

    private int parentID = -1;

    @Getter private String partName;
    public double cubeWidth;
    public double cubeHeight;
    public double cubeDepth;

    private boolean setInParent = false;

    public EntityPart(@Nonnull Entity parent, @Nonnull String partName) {
        super(parent.world);
        this.entityCollisionReduction = 0.5F;
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
        Entity parent = this.getParent();
        if((this.setInParent && parent == null) || (parent != null && parent.isDead)) {
            this.setDead();
        }

        if(!this.setInParent) {
            this.setInParent = parent != null;
            if(parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(EntityComponentTypes.MULTIPART)
                        .ifPresent(multipartEntityComponent -> multipartEntityComponent.entities.add(new MultipartEntityComponent.LinkedEntity(this.partName, this)));
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
