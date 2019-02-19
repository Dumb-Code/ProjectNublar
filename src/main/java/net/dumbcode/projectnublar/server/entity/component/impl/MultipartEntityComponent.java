package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import lombok.Value;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class MultipartEntityComponent implements EntityComponent {

    public List<LinkedEntity> entities = Lists.newArrayList();

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess && !event.getWorld().isRemote) {
            Optional<MultipartEntityComponent> multipart = ((ComponentAccess) entity).get(EntityComponentTypes.MULTIPART);
            if (multipart.isPresent()) {
                MultipartEntityComponent component = multipart.get();
                Dinosaur dinosaur = ((ComponentAccess) entity).getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur;
                List<String> list = dinosaur.getEntityProperties().getLinkedCubeMap().getOrDefault(((ComponentAccess) entity)
                        .get(EntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(dinosaur.getSystemInfo().defaultStage()), Lists.newArrayList());
                for (String s : list) {
                    EntityPart e = new EntityPart(entity, s);
                    e.setPosition(entity.posX, entity.posY, entity.posZ);
                    entity.world.spawnEntity(e);
                    component.entities.add(new LinkedEntity(s, e));
                }
            }
        }
    }

    @Value public static class LinkedEntity { String cubeName; EntityPart entity; }
}
