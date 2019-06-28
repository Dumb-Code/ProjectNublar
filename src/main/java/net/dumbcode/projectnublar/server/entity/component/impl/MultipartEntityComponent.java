package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponent;
import net.dumbcode.dumblibrary.server.entity.component.EntityComponentStorage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.NublarEntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This component allows an Entity
 * to have multiple hit boxes.
 */
@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class MultipartEntityComponent implements EntityComponent {

    public Set<LinkedEntity> entities = Sets.newHashSet();

    public Function<ComponentAccess, List<String>> multipartNames = c -> Lists.newArrayList();

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ComponentAccess && !event.getWorld().isRemote) {
            Optional<MultipartEntityComponent> multipart = ((ComponentAccess) entity).get(NublarEntityComponentTypes.MULTIPART);
            if (multipart.isPresent()) {
                MultipartEntityComponent component = multipart.get();
                for (String s : component.multipartNames.apply((ComponentAccess) entity)) {
                    EntityPart e = new EntityPart(entity, s);
                    e.setPosition(entity.posX, entity.posY, entity.posZ);
                    entity.world.spawnEntity(e);
                    component.entities.add(new LinkedEntity(s, e.getUniqueID()));
                }
            }
        }
        if (event.getWorld().isRemote && entity instanceof EntityPart) {
            Entity parent = ((EntityPart) entity).getParent();
            if (parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(NublarEntityComponentTypes.MULTIPART).ifPresent(c -> c.entities.add(new LinkedEntity(((EntityPart) entity).getPartName(), entity.getUniqueID())));
            }
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (LinkedEntity entity : this.entities) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("partname", entity.cubeName);
            nbt.setUniqueId("uuid", entity.entityUUID);
            list.appendTag(nbt);
        }
        compound.setTag("entities", list);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.entities.clear();
        for (NBTBase base : compound.getTagList("entities", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound nbt = (NBTTagCompound) base;
            this.entities.add(new LinkedEntity(nbt.getString("partname"), nbt.getUniqueId("uuid")));
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.entities.size());
        for (LinkedEntity entity : this.entities) {
            ByteBufUtils.writeUTF8String(buf, entity.cubeName);
            buf.writeLong(entity.entityUUID.getLeastSignificantBits());
            buf.writeLong(entity.entityUUID.getMostSignificantBits());
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        short size = buf.readShort();
        this.entities.clear();
        for (int i = 0; i < size; i++) {
            this.entities.add(new LinkedEntity(ByteBufUtils.readUTF8String(buf), new UUID(buf.readLong(), buf.readLong())));
        }
    }

    //PN only
    @Accessors(chain = true)
    @Setter
    //TODO: have pn extend this class, and have the component attatcher allow for custom storages.
    // WOuld have to create a system for custom storages in the deserilization.
    // could make a whole thing about it in the
    public static class Storage implements EntityComponentStorage<MultipartEntityComponent> {
        private Function<ComponentAccess, List<String>> linkedCubeMap = c -> Lists.newArrayList();

        @Override
        public MultipartEntityComponent construct() {
            MultipartEntityComponent component = new MultipartEntityComponent();
            component.multipartNames = this.linkedCubeMap; //
                        return component;
        }
    }

    @Value
    public static class LinkedEntity {
        String cubeName;
        UUID entityUUID;
    }
}
