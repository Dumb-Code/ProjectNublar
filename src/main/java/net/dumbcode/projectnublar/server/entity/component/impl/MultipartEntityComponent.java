package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * This component allows an Entity
 * to have multiple hit boxes.
 */
@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
@Getter
@Setter
public class MultipartEntityComponent extends EntityComponent {

    private final Set<LinkedEntity> entities = Sets.newHashSet();

    private Function<ComponentAccess, List<String>> multipartNames = c -> Lists.newArrayList();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ComponentAccess && !event.getWorld().isClientSide) {
            Optional<MultipartEntityComponent> multipart = ((ComponentAccess) entity).get(ComponentHandler.MULTIPART);
            if (multipart.isPresent()) {
                MultipartEntityComponent component = multipart.get();
                if(component.getEntities().isEmpty()) {
                    for (String s : component.multipartNames.apply((ComponentAccess) entity)) {
                        EntityPart e = new EntityPart(entity, s);
                        Vector3d position = entity.position();
                        e.setPos(position.x, position.y, position.z);
                        entity.level.addFreshEntity(e);
                        component.entities.add(new LinkedEntity(s, e.getUUID()));
                    }
                }
            }
        }
        if (event.getWorld().isClientSide && entity instanceof EntityPart) {
            Entity parent = ((EntityPart) entity).getParent();
            if (parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(ComponentHandler.MULTIPART).ifPresent(c -> c.entities.add(new LinkedEntity(((EntityPart) entity).getPartName(), entity.getUUID())));
            }
        }
    }

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (LinkedEntity entity : this.entities) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("partname", entity.cubeName);
            nbt.putUUID("uuid", entity.entityUUID);
            list.add(nbt);
        }
        compound.put("entities", list);
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        this.entities.clear();
        for (INBT base : compound.getList("entities", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT nbt = (CompoundNBT) base;
            this.entities.add(new LinkedEntity(nbt.getString("partname"), nbt.getUUID("uuid")));
        }
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeShort(this.entities.size());
        for (LinkedEntity entity : this.entities) {
            buf.writeUtf(entity.cubeName);
            buf.writeUUID(entity.entityUUID);
        }
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        short size = buf.readShort();
        this.entities.clear();
        for (int i = 0; i < size; i++) {
            this.entities.add(new LinkedEntity(buf.readUtf(), buf.readUUID()));
        }
    }

    @Data
    public static class LinkedEntity {
        private final String cubeName;
        private final UUID entityUUID;
        private EntityPart cachedPart;

        public EntityPart getCachedPart(World world) {
            if(this.cachedPart == null) {
                if(world instanceof ServerWorld) {
                    Entity entity = ((ServerWorld) world).getEntity(this.entityUUID);
                    if(entity instanceof EntityPart) {
                        this.cachedPart = (EntityPart) entity;
                    }
                } else if(world instanceof ClientWorld) {
                    for (Entity entity : ((ClientWorld) world).entitiesForRendering()) {
                        if(entity.getUUID().equals(this.entityUUID)) {
                            this.cachedPart = (EntityPart) entity;
                            break;
                        }
                    }
                }
            }
            return cachedPart;
        }
    }
}
