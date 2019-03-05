package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class MultipartEntityComponent implements EntityComponent {

    public Set<LinkedEntity> entities = Sets.newHashSet();

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

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess && !event.getWorld().isRemote) {
            Optional<MultipartEntityComponent> multipart = ((ComponentAccess) entity).get(EntityComponentTypes.MULTIPART);
            if (multipart.isPresent()) {
                MultipartEntityComponent component = multipart.get();
                Dinosaur dinosaur = ((ComponentAccess) entity).getOrExcept(EntityComponentTypes.DINOSAUR).getDinosaur();
                List<String> list = dinosaur.getEntityProperties().getLinkedCubeMap().getOrDefault(((ComponentAccess) entity)
                        .get(EntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(dinosaur.getSystemInfo().defaultStage()), Lists.newArrayList());
                for (String s : list) {
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
}
