package net.dumbcode.projectnublar.server.containers.machines;

import net.dumbcode.projectnublar.server.block.entity.CoalGeneratorBlockEntity;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.minecraft.entity.player.PlayerInventory;

import javax.annotation.Nonnull;

public class CoalGeneratorContainer extends MachineModuleContainer<CoalGeneratorBlockEntity> {
    public CoalGeneratorContainer(int id, PlayerInventory inventory) {
        super(ProjectNublarContainers.MACHINE_MODULES.get(), inventory, id, CoalGeneratorBlockEntity.INFO);
    }

    public CoalGeneratorContainer(int id, PlayerInventory inventory, @Nonnull CoalGeneratorBlockEntity blockEntity) {
        super(ProjectNublarContainers.MACHINE_MODULES.get(), inventory, id, CoalGeneratorBlockEntity.INFO, blockEntity);
    }
}
