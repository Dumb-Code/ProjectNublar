package net.dumbcode.projectnublar.server.entity.component;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityComponentMap extends LinkedHashMap<EntityComponentType<?>, EntityComponent> {

    @Override
    public EntityComponent put(EntityComponentType<?> key, EntityComponent value) {
        value.onAdded(this);
        return super.put(key, value);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> T getNullable(EntityComponentType<T> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return (T) component;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityComponent> Optional<T> get(EntityComponentType<T> type) {
        EntityComponent component = super.get(type);
        if (component != null) {
            return Optional.of((T) component);
        }
        return Optional.empty();
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
        //Leave the deserialization until all other components are added
        List<Pair<EntityComponent, NBTTagCompound>> components = Lists.newArrayList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound componentTag = list.getCompoundTagAt(i);
            ResourceLocation identifier = new ResourceLocation(componentTag.getString("identifier"));
            EntityComponentType<?> componentType = ProjectNublar.COMPONENT_REGISTRY.getValue(identifier);
            if (componentType != null) {
                EntityComponent component = componentType.construct();
                this.put(componentType, component);
                components.add(Pair.of(component, componentTag));
            } else {
                ProjectNublar.getLogger().warn("Skipped invalid entity component: '{}'", identifier);
            }
        }

        for (Pair<EntityComponent, NBTTagCompound> component : components) {
            component.getLeft().deserialize(component.getRight());
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
