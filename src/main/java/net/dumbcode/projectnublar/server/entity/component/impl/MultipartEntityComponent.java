package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
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

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class MultipartEntityComponent implements EntityComponent {

    public Set<LinkedEntity> entities = Sets.newHashSet();

    public Function<ComponentAccess, List<String>> multipartNames = c -> Lists.newArrayList();

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

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess && !event.getWorld().isRemote) {
            Optional<MultipartEntityComponent> multipart = ((ComponentAccess) entity).get(EntityComponentTypes.MULTIPART);
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
        if(event.getWorld().isRemote && entity instanceof EntityPart) {
            Entity parent = ((EntityPart) entity).getParent();
            if(parent instanceof ComponentAccess) {
                ((ComponentAccess) parent).get(EntityComponentTypes.MULTIPART).ifPresent(c -> c.entities.add(new LinkedEntity(((EntityPart) entity).getPartName(), entity.getUniqueID())));
            }
        }
    }

    @Value public static class LinkedEntity { String cubeName; UUID entityUUID; }

    //PN only
    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<MultipartEntityComponent> {
        private Map<ModelStage, List<String>> linkedCubeMap = Maps.newEnumMap(ModelStage.class);

        private ModelStage defaultStage;

        @Override
        public MultipartEntityComponent construct() {
            MultipartEntityComponent component = new MultipartEntityComponent();
            component.multipartNames = access -> this.linkedCubeMap.getOrDefault(access.get(EntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(access.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur.getSystemInfo().defaultStage()), Lists.newArrayList());
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            JsonElement entityMap = json.get("entity_map");
            if(entityMap instanceof JsonObject) {
                JsonObject entityobj = entityMap.getAsJsonObject();
                for (ModelStage stage : ModelStage.values()) {
                    if(JsonUtils.isJsonArray(entityobj, stage.getName())) {
                        this.linkedCubeMap.computeIfAbsent(stage, m -> Lists.newArrayList())
                                .addAll(StreamSupport.stream(entityobj.getAsJsonArray(stage.getName()).spliterator(), false)
                                        .filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).collect(Collectors.toList()));
                    }
                }
            }
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonObject entity = new JsonObject();
            for (Map.Entry<ModelStage, List<String>> entry : this.linkedCubeMap.entrySet()) {
                List<String> value = entry.getValue();
                if(value != null && !value.isEmpty()) {
                    JsonArray obj = new JsonArray();
                    for (String s : entry.getValue()) {
                        obj.add(s);
                    }
                    entity.add(entry.getKey().getName(), obj);
                }
            }
            json.add("entity_map", entity);
        }
    }
}
