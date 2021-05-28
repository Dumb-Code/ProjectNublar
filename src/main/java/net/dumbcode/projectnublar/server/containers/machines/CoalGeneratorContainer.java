package net.dumbcode.projectnublar.server.containers.machines;

import net.dumbcode.projectnublar.server.block.entity.CoalGeneratorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoalGeneratorContainer extends MachineModuleContainer<CoalGeneratorBlockEntity> {
    public CoalGeneratorContainer(int id, PlayerInventory inventory) {
        super(ProjectNublarContainers.COAL_GENERATOR.get(), inventory, id, CoalGeneratorBlockEntity.INFO);
    }

    public CoalGeneratorContainer(int id, PlayerInventory inventory, @Nonnull CoalGeneratorBlockEntity blockEntity) {
        super(ProjectNublarContainers.COAL_GENERATOR.get(), inventory, id, CoalGeneratorBlockEntity.INFO, blockEntity);
    }
}
