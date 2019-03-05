package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.HerdComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ProjectNublar.MODID)
public class HerdSystem implements EntitySystem {

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

            if(!herd.inHerd) {
                for (Entity foundEntity : entity.world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(entity.getPosition()).expand(50, 50, 50), e -> e instanceof ComponentAccess)) {
                    ComponentAccess ca = (ComponentAccess) foundEntity;
                    HerdComponent foundHerd = ca.getOrNull(EntityComponentTypes.HERD);
                    if(foundHerd != null && foundHerd.herd != null) {
                        foundHerd.herd.addMember(entity.getUniqueID(), herd);
                    }
                }
            }

        }
    }


    @SubscribeEvent
    public static void onEntityDie(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if(entity instanceof ComponentAccess) {
            HerdComponent component = ((ComponentAccess) entity).getOrNull(EntityComponentTypes.HERD);
            if(component != null) {
                List<HerdComponent> members = component.getComponents(entity.world);
                if(!members.isEmpty()) {
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
