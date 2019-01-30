package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.dumbcode.dumblibrary.server.ai.AdvancedAIBase;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Queue;

public class AdvancedAIComponent implements EntityComponent
{

    public final List<AdvancedAIBase> tasks = Lists.newLinkedList();
    public final Queue<AdvancedAIBase> currentTasks = Queues.newConcurrentLinkedQueue();
    public final int updateRate = 20;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound)
    {
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound)
    {

    }
}
