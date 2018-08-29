package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentMap;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Map;

public class ComposableCreatureEntity extends EntityCreature implements ComponentAccess {
    private final EntityComponentMap components = new EntityComponentMap();

    public ComposableCreatureEntity(World world) {
        super(world);
    }

    public <T extends EntityComponent> void attachComponent(EntityComponentType<T> type, T component) {
        this.components.put(type, component);
    }

    @Nullable
    @Override
    public <T extends EntityComponent> T getOrNull(EntityComponentType<T> type) {
        return this.components.get(type);
    }

    @Override
    public boolean contains(EntityComponentType<?> type) {
        return this.components.containsKey(type);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);

        NBTTagList componentList = new NBTTagList();
        for (Map.Entry<EntityComponentType<?>, EntityComponent> entry : this.components.entrySet()) {
            NBTTagCompound componentTag = entry.getValue().serialize(new NBTTagCompound());
            componentTag.setString("identifier", entry.getKey().getIdentifier().toString());

            componentList.appendTag(componentTag);
        }

        compound.setTag("components", componentList);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagList componentList = compound.getTagList("components", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < componentList.tagCount(); i++) {
            NBTTagCompound componentTag = componentList.getCompoundTagAt(i);
            ResourceLocation identifier = new ResourceLocation(componentTag.getString("identifier"));
            EntityComponentType<?> componentType = ProjectNublar.COMPONENT_REGISTRY.getValue(identifier);
            if (componentType != null) {
                EntityComponent component = componentType.construct();
                component.deserialize(componentTag);
                this.components.put(componentType, component);
            } else {
                ProjectNublar.getLogger().warn("Skipped invalid entity component: '{}'", identifier);
            }
        }
    }
}
