package net.dumbcode.projectnublar.server.entity;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentMap;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.dumbcode.projectnublar.server.entity.component.impl.AiComponent;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ComposableCreatureEntity extends EntityCreature implements ComponentWriteAccess, IEntityAdditionalSpawnData {
    private final EntityComponentMap components = new EntityComponentMap();

    public ComposableCreatureEntity(World world) {
        super(world);
        if(!this.world.isRemote) {
            this.attachComponents();
            for (EntityComponent component : this.components.values()) {
                if (component instanceof AiComponent) {
                    AiComponent aiComponent = (AiComponent) component;
                    aiComponent.apply(this.tasks, this);
                }
            }
        }
    }


    protected void attachComponents() {
    }

    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> void attachComponent(EntityComponentType<T, S> type, T component) {
        if(component == null) {
            throw new NullPointerException("Component on type " + type.getIdentifier() + " is null.");
        }
        this.components.put(type, component);
    }

    @Nullable
    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrNull(EntityComponentType<T, S> type) {
        return this.components.getNullable(type);
    }

    @Nonnull
    @Override
    public <T extends EntityComponent, S extends EntityComponentStorage<T>> T getOrExcept(EntityComponentType<T, S> type) {
        T component = this.components.getNullable(type);
        if (component == null) {
            throw new IllegalArgumentException("Component '" + type.getIdentifier() + "' is not present on entity");
        }
        return component;
    }

    @Override
    public boolean contains(EntityComponentType<?, ?> type) {
        return this.components.containsKey(type);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("components", this.components.serialize(new NBTTagList()));

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagList componentList = compound.getTagList("components", Constants.NBT.TAG_COMPOUND);
        this.components.deserialize(componentList);
    }

    @Override
    public void writeSpawnData(ByteBuf buf) {
        this.components.serialize(buf);
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        this.components.deserialize(buf);
    }
}
