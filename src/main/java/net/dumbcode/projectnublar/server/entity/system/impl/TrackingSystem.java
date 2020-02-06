package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.TrackingComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum TrackingSystem implements EntitySystem {
    INSTANCE;

    private Entity[] entities = new Entity[0];
    private TrackingComponent[] components = new TrackingComponent[0];

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(ComponentHandler.TRACKING_DATA);
        Entity[] newEntities = family.getEntities();
        Set<Entity> entitySet = Arrays.stream(newEntities).collect(Collectors.toSet());
        if(newEntities.length < this.entities.length) { //Entity removed
            TrackingSavedData data = TrackingSavedData.getData(this.entities[0].world);
            Arrays.stream(this.entities).filter(e -> !entitySet.contains(e)).forEach(e -> data.removeEntry(e.getUniqueID()));
        }
        this.entities = newEntities;
        this.components = family.populateBuffer(ComponentHandler.TRACKING_DATA, this.components);
    }

    @Override
    public void update(World world) {
        for (int i = 0; i < this.entities.length; i++) {
            TrackingSavedData.DataEntry entry = new TrackingSavedData.DataEntry(this.entities[i].getUniqueID(), this.entities[i].getPosition());
            this.components[i].getInfoSuppliers().stream().map(Supplier::get).filter(Objects::nonNull).forEach(entry.getInformation()::add);
            TrackingSavedData.getData(world).setEntry(entry);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onLivingDeath(LivingDeathEvent event) {
        TrackingSavedData.getData(event.getEntity().world).removeEntry(event.getEntity().getUniqueID());
    }
}
