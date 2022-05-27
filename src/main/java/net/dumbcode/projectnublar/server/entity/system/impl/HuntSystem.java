package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ecs.EntityFamily;
import net.dumbcode.dumblibrary.server.ecs.EntityManager;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.impl.HerdComponent;
import net.dumbcode.dumblibrary.server.ecs.system.EntitySystem;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.HuntComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HuntSystem implements EntitySystem {

    private Entity[] entities = new Entity[0];
    private HerdComponent[] herds = new HerdComponent[0];
    private MetabolismComponent[] metabolism = new MetabolismComponent[0];
    private HuntComponent[] hunts = new HuntComponent[0];

    //A map of HerdUUID -> [member indexes]
    private final Map<UUID, List<Integer>> herdIndexes = new HashMap<>();

    @Override
    public void populateEntityBuffers(EntityManager manager) {
        EntityFamily<Entity> family = manager.resolveFamily(EntityComponentTypes.HERD, ComponentHandler.METABOLISM, ComponentHandler.HUNTING);
        this.entities = family.getEntities();
        this.herds = family.populateBuffer(EntityComponentTypes.HERD, this.herds);
        this.hunts = family.populateBuffer(ComponentHandler.HUNTING, this.hunts);
        this.metabolism = family.populateBuffer(ComponentHandler.METABOLISM, this.metabolism);

        this.herdIndexes.clear();
        this.herdIndexes.putAll(IntStream.range(0, this.herds.length).boxed().collect(Collectors.groupingBy(i -> this.herds[i].herdUUID)));
    }

    @Override
    public void update(World world) {
        for (UUID herdUUID : this.herdIndexes.keySet()) {
            List<Integer> members = this.herdIndexes.get(herdUUID);

            List<Integer> hungry = members.stream()
                .filter(m -> isHungry(this.metabolism[m]))
                .filter(m -> !this.hunts[m].isInHunt)
                .collect(Collectors.toList());

            if((float) hungry.size() / members.size() >= 0.6F) {
                //Start the hunt
                int leader = world.random.nextInt(hungry.size());

                Entity leaderEntity = this.entities[leader];

                this.hunts[leader].wantsToStartHunt = true;

                for (Integer member : hungry) {
                    HuntComponent hunt = this.hunts[member];
                    if(leader != member) {
                        hunt.followingHuntLeader = leaderEntity.getUUID();
                    }
                    hunt.huntStartPosition = this.entities[member].blockPosition();
                    hunt.isInHunt = true;
                }


            }
        }
    }
    
    private boolean isHungry(MetabolismComponent component) {
        return component.getFood() <= 3000;
    }
}
