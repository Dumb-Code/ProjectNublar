package net.dumbcode.projectnublar.server.entity;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentMap;
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

public abstract class ComposableCreatureEntity extends EntityCreature implements ComponentAccess, IEntityAdditionalSpawnData {
    private final EntityComponentMap components = new EntityComponentMap();

    public ComposableCreatureEntity(World world) {
        super(world);
        this.attachComponentAI();
    }

    protected void attachComponentAI() {
        super.initEntityAI();
        for (EntityComponent component : this.components.values()) {
            if (component instanceof AiComponent) {
                AiComponent aiComponent = (AiComponent) component;
                aiComponent.apply(this.tasks);
            }
        }
    }

    protected abstract void attachComponents();

    public <T extends EntityComponent> void attachComponent(EntityComponentType<T> type, T component) {
        this.components.put(type, component);
    }

    public <T extends EntityComponent> void attachComponent(EntityComponentType<T> type) {
        this.components.put(type, type.construct());
    }

    @Nullable
    @Override
    public <T extends EntityComponent> T getOrNull(EntityComponentType<T> type) {
        return this.components.get(type);
    }

    @Nonnull
    @Override
    public <T extends EntityComponent> T getOrExcept(EntityComponentType<T> type) {
        T component = this.components.get(type);
        if (component == null) {
            throw new IllegalArgumentException("Component '" + type.getIdentifier() + "' is not present on entity");
        }
        return component;
    }

    @Override
    public boolean contains(EntityComponentType<?> type) {
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
