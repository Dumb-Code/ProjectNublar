package net.dumbcode.projectnublar.server.entity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentType;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public interface EntityManager extends ICapabilityProvider {
    @SubscribeEvent
    static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        EntityManager capability = new Impl();
        event.addCapability(new ResourceLocation(ProjectNublar.MODID, "entity_manager"), capability);
    }

    void addEntity(Entity entity);

    void removeEntity(Entity entity);

    void populateSystemBuffers();

    <T extends EntityComponent> T[] populate(EntityComponentType<T> type);

    class Impl implements EntityManager {
        private final List<ComponentAccess> managedEntities = new ArrayList<>();
        private final List<EntitySystem> systems = new ArrayList<>();

        @Override
        public void addEntity(Entity entity) {
            if (entity instanceof ComponentAccess) {
                this.managedEntities.add((ComponentAccess) entity);
                this.populateSystemBuffers();
            }
        }

        @Override
        public void removeEntity(Entity entity) {
            if (entity instanceof ComponentAccess) {
                this.managedEntities.remove(entity);
                this.populateSystemBuffers();
            }
        }

        @Override
        public void populateSystemBuffers() {
            for (EntitySystem system : this.systems) {
                system.populateBuffers(this);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends EntityComponent> T[] populate(EntityComponentType<T> type) {
            // TODO: Ideally don't want to reallocate arrays if the size hasn't changed
            List<T> components = new ArrayList<>(this.managedEntities.size());
            for (ComponentAccess entity : this.managedEntities) {
                T component = entity.getOrNull(type);
                if (component != null) {
                    components.add(component);
                }
            }
            return (T[]) components.toArray();
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
