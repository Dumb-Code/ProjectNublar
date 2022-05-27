package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.projectnublar.server.entity.component.impl.HuntComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;

import java.util.OptionalDouble;
import java.util.logging.Level;

public class HuntFollowGoal extends EntityGoal {

    private final CreatureEntity entity;
    private final MetabolismComponent metabolismComponent;
    private final HuntComponent component;

    public HuntFollowGoal(CreatureEntity entity, GoalManager manager, MetabolismComponent metabolismComponent, HuntComponent component) {
        super(manager);
        this.entity = entity;
        this.metabolismComponent = metabolismComponent;
        this.component = component;
    }

    @Override
    protected void tick() {

    }

    @Override
    public void onFinished() {
        this.component.isInHunt = false;
    }

    @Override
    protected OptionalDouble getImportance() {
        if(this.component.followingHuntLeader != null) {
            Entity entity = ((ServerWorld) this.entity.level).getEntity(this.component.followingHuntLeader);
            if(entity != null && entity.distanceTo(this.entity) <= 150) {
                return OptionalDouble.of(5000);
            }
        }
        return OptionalDouble.empty();
    }
}
