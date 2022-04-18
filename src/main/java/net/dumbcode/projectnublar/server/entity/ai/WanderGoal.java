package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.OptionalDouble;
import java.util.Random;

public class WanderGoal extends EntityGoal {

    private final WanderComponent component;
    private final CreatureEntity mob;

    public WanderGoal(GoalManager manager, WanderComponent component, CreatureEntity mob) {
        super(manager);
        this.component = component;
        this.mob = mob;
    }

    protected Vector3d getPosition() {
        Vector3d pos = RandomPositionGenerator.getPos(this.mob, 10, 7);

        if(this.component.isAvoidWater()) {
            if (this.mob.isInWater()) {
                Vector3d vec3d = RandomPositionGenerator.getLandPos(this.mob, 15, 7);
                return vec3d == null ? pos : vec3d;
            } else {
                return this.mob.getRandom().nextFloat() >= 0.001 ? RandomPositionGenerator.getLandPos(this.mob, 10, 7) : pos;
            }
        } else {
            return pos;
        }
    }

    @Override
    protected void tick() {
        if(!this.mob.isPathFinding()) {
            this.finish();
        }
    }

    @Override
    public boolean onStarted() {
        Vector3d target = this.getPosition();
        if(target == null) {
            return false;
        }
        this.mob.getNavigation().moveTo(target.x, target.y, target.z, this.component.getSpeed());
        return true;
    }

    @Override
    protected OptionalDouble getImportance() {
        if(RANDOM.nextInt(this.component.getChance()) == 0) {
            return OptionalDouble.of(1);
        }
        return OptionalDouble.empty();
    }
}
