package net.dumbcode.projectnublar.server.entity.component;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityComponentMap extends LinkedHashMap<EntityComponentType<?>, EntityComponent> {
    public <T extends EntityComponent> void put(EntityComponentType<T> type, T component) {
        super.put(type, component);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T get(EntityComponentType<T> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return (T) component;
        }
        return null;
    }

    public NBTTagList serialize(NBTTagList list) {
        for (Map.Entry<EntityComponentType<?>, EntityComponent> entry : this.entrySet()) {
            NBTTagCompound componentTag = entry.getValue().serialize(new NBTTagCompound());
            componentTag.setString("identifier", entry.getKey().getIdentifier().toString());
            list.appendTag(componentTag);
        }
        return list;
    }

    public void deserialize(NBTTagList list) {
        this.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound componentTag = list.getCompoundTagAt(i);
            ResourceLocation identifier = new ResourceLocation(componentTag.getString("identifier"));
            EntityComponentType<?> componentType = ProjectNublar.COMPONENT_REGISTRY.getValue(identifier);
            if (componentType != null) {
                EntityComponent component = componentType.construct();
                component.deserialize(componentTag);
                this.put(componentType, component);
            } else {
                ProjectNublar.getLogger().warn("Skipped invalid entity component: '{}'", identifier);
            }
        }
    }

    public void serialize(ByteBuf buf) {
        for (EntityComponent component : this.values()) {
            component.serialize(buf);
        }
    }

    public void deserialize(ByteBuf buf) {
        for (EntityComponent component : this.values()) {
            component.deserialize(buf);
        }
    }
}
