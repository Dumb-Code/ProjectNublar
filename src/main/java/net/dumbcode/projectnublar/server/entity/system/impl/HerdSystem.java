package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.HerdComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public enum HerdSystem implements EntitySystem {

    INSTANCE;

    private Entity[] matchedEntities = new Entity[0];
    private HerdComponent[] herds = new HerdComponent[0];

    @Override
    public void populateBuffers(EntityManager manager) {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.HERD);
        this.herds = family.populateBuffer(EntityComponentTypes.HERD, this.herds);
        this.matchedEntities = family.getEntities();
    }

    @Override
    public void update() {
        for (int i = 0; i < this.herds.length; i++) {
            Entity entity = this.matchedEntities[i];
            HerdComponent herd = this.herds[i];

            if(herd.herdUUID == null) {
                for (Entity foundEntity : entity.world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(entity.getPosition()).grow(50, 50, 50), e -> e instanceof ComponentAccess
                        && ((ComponentAccess) e).get(EntityComponentTypes.HERD).map(c -> c.herdTypeID.equals(herd.herdTypeID)).orElse(false))) {

                    ComponentAccess ca = (ComponentAccess) foundEntity;
                    HerdComponent foundHerd = ca.getOrNull(EntityComponentTypes.HERD);
                    if(foundHerd != null && foundHerd.herd != null) {
                        foundHerd.herd.addMember(entity.getUniqueID(), herd);
                        break;
                    }
                }

                //Create the herd
                if(herd.herdUUID == null) {
                    herd.herd = herd;
                    herd.leader = true;
                    herd.herdUUID = UUID.randomUUID();
                    herd.members.add(entity.getUniqueID());
                    System.out.println("HERD CREATED");
                }
            } else {
                if(herd.herd == null) {
                    if(herd.leader) {
                        herd.herd = herd;
                    } else {
                        for (Entity e : entity.world.loadedEntityList) {
                            if(e != entity && e instanceof ComponentAccess) {
                                ComponentAccess ca = (ComponentAccess) e;
                                HerdComponent herdComponent = ca.getOrNull(EntityComponentTypes.HERD);
                                if(herdComponent != null && herdComponent.herdUUID != null && herdComponent.herdUUID.equals(herd.herdUUID) && herdComponent.herd != null) {
                                    herdComponent.herd.addMember(entity.getUniqueID(), herd);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(herd.herd != null) {
                    for (Entity e : entity.world.loadedEntityList) {
                        if(e instanceof ComponentAccess) {
                            HerdComponent herdComponent = ((ComponentAccess) e).getOrNull(EntityComponentTypes.HERD);
                            if(e.getDistance(entity) > 30 && herdComponent != null && herdComponent.leader && herdComponent.herdUUID.equals(herd.herdUUID) && herd.tryMoveCooldown <= 0) {
                                ((EntityLiving) entity).getNavigator().tryMoveToEntityLiving(e, 0.1F);
                                herd.tryMoveCooldown = 120;
                                break;
                            }

                        }
                    }
                    herd.tryMoveCooldown--;
                }
            }
        }
    }


    @SubscribeEvent
    public static void onEntityDie(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess) {
            HerdComponent component = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.HERD);
            if(component != null && component.herd == component) {
                List<HerdComponent> members = component.getComponents(entity.world);
                if(!members.isEmpty()) {
                    for (HerdComponent member : members) {
                        member.herd = members.get(0);
                    }
                    members.get(0).leader = true;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDamaged(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        Entity source = event.getSource().getTrueSource();
        if(source !=  null && entity instanceof ComponentAccess) {
            HerdComponent component = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.HERD);
            if(component != null && component.herd != null) {
                component.herd.enemies.add(source.getUniqueID());
            }
        }
    }
}
