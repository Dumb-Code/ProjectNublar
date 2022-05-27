package net.dumbcode.projectnublar.server.entity.ai;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.projectnublar.server.entity.component.impl.HuntComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MetabolismComponent;
import net.minecraft.entity.CreatureEntity;

import java.util.OptionalDouble;

public class HuntLeadGoal extends EntityGoal {

    private final CreatureEntity entity;
    private final MetabolismComponent metabolismComponent;
    private final HuntComponent component;

    public HuntLeadGoal(CreatureEntity entity, GoalManager manager, MetabolismComponent metabolismComponent, HuntComponent component) {
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
        if(this.component.wantsToStartHunt) {
            return OptionalDouble.of(50000);
        }
        return OptionalDouble.empty();
    }
}
