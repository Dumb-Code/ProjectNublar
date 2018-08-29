package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.system.RegisterSystemsEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public interface EntityManager extends ICapabilityProvider {
    @SubscribeEvent
    static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        EntityManager.Impl capability = new EntityManager.Impl();
        event.addCapability(new ResourceLocation(ProjectNublar.MODID, "entity_manager"), capability);

        MinecraftForge.EVENT_BUS.post(new RegisterSystemsEvent(event.getObject(), capability.systems));
    }

    @SubscribeEvent
    static void onWorldTick(TickEvent.WorldTickEvent event) {
        // TODO: We probably need to run this when entity ticking starts
        if (event.phase == TickEvent.Phase.START) {
            EntityManager entityManager = event.world.getCapability(ProjectNublar.ENTITY_MANAGER, null);
            if (entityManager != null) {
                entityManager.updateSystems();
            }
        }
    }

    void updateSystems();

    void addEntity(Entity entity);

    void removeEntity(Entity entity);

    EntityFamily resolveFamily(EntityComponentType<?> types);

    class Impl implements EntityManager {
        private final List<Entity> managedEntities = new ArrayList<>();
        private final List<EntitySystem> systems = new ArrayList<>();
        private boolean systemsDirty = true;

        @Override
        public void updateSystems() {
            if (this.systemsDirty) {
                this.systemsDirty = false;
                for (EntitySystem system : this.systems) {
                    system.populateBuffers(this);
                }
            }
            for (EntitySystem system : this.systems) {
                system.update();
            }
        }

        @Override
        public void addEntity(Entity entity) {
            if (entity instanceof ComponentAccess) {
                this.managedEntities.add(entity);
                this.systemsDirty = true;
            }
        }

        @Override
        public void removeEntity(Entity entity) {
            if (entity instanceof ComponentAccess) {
                this.managedEntities.remove(entity);
                this.systemsDirty = true;
            }
        }

        @Override
        public EntityFamily resolveFamily(EntityComponentType<?> types) {
            List<Entity> entities = new ArrayList<>(this.managedEntities.size());
            for (Entity entity : this.managedEntities) {
                if (((ComponentAccess) entity).matchesAll(types)) {
                    entities.add(entity);
                }
            }
            return new EntityFamily(entities.toArray(new Entity[0]));
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == ProjectNublar.ENTITY_MANAGER;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == ProjectNublar.ENTITY_MANAGER) {
                return ProjectNublar.ENTITY_MANAGER.cast(this);
            }
            return null;
        }
    }
}
