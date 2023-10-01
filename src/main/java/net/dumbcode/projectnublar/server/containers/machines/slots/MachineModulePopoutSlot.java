package net.dumbcode.projectnublar.server.containers.machines.slots;

import lombok.Getter;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.ITextComponent;

@Getter
public class MachineModulePopoutSlot extends MachineModuleSlot {
    private final int visualShowX;
    private final int visualShowY;
    private final Component textComponent;

    public MachineModulePopoutSlot(MachineModuleBlockEntity<?> blockEntity, int index, int xPosition, int yPosition, int visualShowX, int visualShowY, ITextComponent textComponent) {
        super(blockEntity, index, xPosition, yPosition);
        this.visualShowX = visualShowX;
        this.visualShowY = visualShowY;
        this.textComponent = textComponent;
    }
}
