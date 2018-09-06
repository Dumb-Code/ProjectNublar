package net.dumbcode.projectnublar.server.containers.machines;

import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.minecraft.entity.player.EntityPlayer;

public class FossilProcessorContainer extends MachineModuleContainer {

    private final EntityPlayer player;
    private final FossilProcessorBlockEntity blockEntity;

    public FossilProcessorContainer(EntityPlayer player, FossilProcessorBlockEntity blockEntity) {
        this.player = player;
        this.blockEntity = blockEntity;

        this.addSlotToContainer(new MachineModuleSlot(this.blockEntity, 0, 8, 116)); //water
        this.addSlotToContainer(new MachineModuleSlot(this.blockEntity, 1, 100, 50)); //fossil
        this.addSlotToContainer(new MachineModuleSlot(this.blockEntity, 2, 150, 50)); //test tube
        this.addSlotToContainer(new MachineModuleSlot(this.blockEntity, 3, 100, 100)); //Filter
        this.addSlotToContainer(new MachineModuleSlot(this.blockEntity, 4, 150, 100)); //output

        this.addPlayerSlots(player, 138);
    }
}
