package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public class WanderAI extends RandomWalkingGoal {

    private final WanderComponent component;

    public WanderAI(CreatureEntity creatureIn, WanderComponent component) {
        super(creatureIn, component.getSpeed(), component.getChance());
        this.component = component;
    }

    @Nullable
    @Override
    protected Vector3d getPosition() {
        if(this.component.getCanExecute().get()) {
            if(this.component.isAvoidWater()) {
                if (this.mob.isInWater()) {
                    Vector3d vec3d = RandomPositionGenerator.getLandPos(this.mob, 15, 7);
                    return vec3d == null ? super.getPosition() : vec3d;
                } else {
                    return this.mob.getRandom().nextFloat() >= 0.001 ? RandomPositionGenerator.getLandPos(this.mob, 10, 7) : super.getPosition();
                }
            } else {
                return super.getPosition();
            }
        }
        return null;
    }

}
