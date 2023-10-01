package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.dumblibrary.server.ai.EntityGoal;
import net.dumbcode.dumblibrary.server.ai.GoalManager;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.EntityGoalSupplier;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.ai.HuntFollowGoal;
import net.dumbcode.projectnublar.server.entity.ai.HuntLeadGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.core.BlockPos;

import java.util.UUID;
import java.util.function.Consumer;

public class HuntComponent extends EntityComponent implements EntityGoalSupplier {
    public boolean wantsToStartHunt;
    public boolean isInHunt;
    public UUID followingHuntLeader;

    public BlockPos huntStartPosition = BlockPos.ZERO;

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putBoolean("wants_to_start_hunt", this.wantsToStartHunt);
        if(this.followingHuntLeader != null) {
            compound.putUUID("following_hunt_leader", this.followingHuntLeader);
        }
        compound.putInt("hunt_start_x", this.huntStartPosition.getX());
        compound.putInt("hunt_start_y", this.huntStartPosition.getY());
        compound.putInt("hunt_start_z", this.huntStartPosition.getZ());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        this.wantsToStartHunt = compound.getBoolean("wants_to_start_hunt");
        if(compound.hasUUID("following_hunt_leader")) {
            this.followingHuntLeader = compound.getUUID("following_hunt_leader");
        }
        this.huntStartPosition = new BlockPos(
            compound.getInt("hunt_start_x"),
            compound.getInt("hunt_start_y"),
            compound.getInt("hunt_start_z")
        );
        super.deserialize(compound);
    }

    @Override
    public void addGoals(GoalManager manager, Consumer<EntityGoal> consumer, ComponentAccess access) {
        if(access instanceof CreatureEntity) {
            access.get(ComponentHandler.METABOLISM).ifPresent(metabolismComponent -> {
                consumer.accept(new HuntLeadGoal((CreatureEntity) access, manager, metabolismComponent, this));
                consumer.accept(new HuntFollowGoal((CreatureEntity) access, manager, metabolismComponent, this));
            });
        }
    }
}
