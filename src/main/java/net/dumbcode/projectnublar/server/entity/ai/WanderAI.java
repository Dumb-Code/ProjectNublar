package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.projectnublar.server.entity.component.impl.ai.WanderComponent;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class WanderAI extends EntityAIWander {

    private final WanderComponent component;

    public WanderAI(EntityCreature creatureIn, WanderComponent component) {
        super(creatureIn, component.getSpeed(), component.getChance());
        this.component = component;
        this.setMutexBits(1);
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        if(this.component.getCanExecute().get()) {
            if(this.component.isAvoidWater()) {
                if (this.entity.isInWater()) {
                    Vec3d vec3d = RandomPositionGenerator.getLandPos(this.entity, 15, 7);
                    return vec3d == null ? super.getPosition() : vec3d;
                } else {
                    return this.entity.getRNG().nextFloat() >= 0.001 ? RandomPositionGenerator.getLandPos(this.entity, 10, 7) : super.getPosition();
                }
            } else {
                return super.getPosition();
            }
        }
        return null;
    }

}
