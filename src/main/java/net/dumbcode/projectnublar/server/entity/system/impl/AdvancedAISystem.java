package net.dumbcode.projectnublar.server.entity.system.impl;

import net.dumbcode.dumblibrary.server.ai.AIType;
import net.dumbcode.dumblibrary.server.ai.AdvancedAIBase;
import net.dumbcode.projectnublar.server.entity.EntityFamily;
import net.dumbcode.projectnublar.server.entity.EntityManager;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AdvancedAIComponent;
import net.dumbcode.projectnublar.server.entity.system.EntitySystem;
import net.minecraft.entity.Entity;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

public class AdvancedAISystem implements EntitySystem
{

    private AdvancedAIComponent[] advancedAI = new AdvancedAIComponent[0];
    private Entity[] entities = new Entity[0];

    @Override
    public void populateBuffers(EntityManager manager)
    {
        EntityFamily family = manager.resolveFamily(EntityComponentTypes.ADVANCED_AI);
        advancedAI = family.populateBuffer(EntityComponentTypes.ADVANCED_AI, advancedAI);
        entities = family.getEntities();
    }

    @Override
    public void update()
    {
        for (int i = 0; i < advancedAI.length; i++)
        {
            Entity entity = entities[i];
            AdvancedAIComponent ai = advancedAI[i];

            List<AdvancedAIBase> tasks = ai.tasks;
            Queue<AdvancedAIBase> currentTasks = ai.currentTasks;

            if (entity.ticksExisted % ai.updateRate == 0) {
                tasks.forEach(AdvancedAIBase::checkImportance);
                tasks.sort(Comparator.comparing(AdvancedAIBase::getImportance).reversed());
                for (AdvancedAIBase task : tasks) {
                    if (task.isUsesCooldown()) {
                        task.tickCooldown();
                    }
                    if (task.shouldExecute() && currentTasks.isEmpty()) {
                        currentTasks.add(task);
                    } else if (task.shouldExecute() && !currentTasks.isEmpty()) {
                        if (currentTasks.peek().isConcurrent() && (task.isConcurrent() || task.getType().equals(AIType.ANIMATION))) {
                            currentTasks.add(task);
                        }
                    }
                }
                if (!currentTasks.isEmpty()) {
                    for (AdvancedAIBase task : currentTasks) {
                        if (task.isFinished()) {
                            currentTasks.remove(task);
                        } else {
                            if (task.shouldContinue()) {
                                task.execute();
                            }
                            if (task.isUpdatable()) {
                                task.update();
                            }
                        }
                    }
                }
            }
        }
    }
}
