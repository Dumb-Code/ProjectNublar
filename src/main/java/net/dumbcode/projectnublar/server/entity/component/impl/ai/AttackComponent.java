package net.dumbcode.projectnublar.server.entity.component.impl.ai;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.ComposableCreatureEntity;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherEnemiesComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.GatherGeneticsComponent;
import net.dumbcode.projectnublar.server.entity.ai.EntityAttackAI;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurDropsComponent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AttackComponent extends EntityComponent implements FinalizableComponent {

    private final int priority = 1;
    private final double speed = 0.7D;

    @Getter private List<Class<? extends EntityLiving>> enemies = new ArrayList<>();

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        for (EntityComponent component : entity.getAllComponents()) {
            if(component instanceof GatherEnemiesComponent) {
                enemies.addAll(((GatherEnemiesComponent) component).gatherEnemies());
            }
        }
        if(entity instanceof ComposableCreatureEntity) {
            ComposableCreatureEntity creature = (ComposableCreatureEntity) entity;
            creature.tasks.addTask(this.priority, new EntityAttackAI(creature, enemies, speed));
        } else {
            throw new IllegalArgumentException("Tried to attach a attack component to an ecs of class " + entity.getClass() + ". The given ecs must be a subclass of EntityCreature");
        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {

    }

    @Accessors(chain = true)
    @Setter
    @Getter
    public static class Storage implements EntityComponentStorage<AttackComponent> {

        private List<Class<? extends EntityLiving>> enemies = new ArrayList<>();
        
        @Override
        public AttackComponent construct() {
            AttackComponent component = new AttackComponent();
            component.getEnemies().addAll(enemies);
            return component;
        }

        @SafeVarargs
        public final Storage addEnemies(Class<? extends EntityLiving>... enemies) {
            Collections.addAll(this.enemies, enemies);
            return this;
        }
    }
}
