package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class UnincubatedEggItem extends DnaHoverDinosaurItem {

    public UnincubatedEggItem(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(dinosaur, translationKey, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> lines, ITooltipFlag flag) {
        lines.add(new StringTextComponent(MathUtils.ensureTrailingZeros(stack.getOrCreateTagElement(ProjectNublar.MODID).getFloat("AmountDone"), 1) + "%"));
        Screen screen = Minecraft.getInstance().screen;
        if(screen instanceof MachineContainerScreen) {
            MachineModuleBlockEntity<?> blockEntity = ((MachineContainerScreen) screen).getMenu().getBlockEntity();
            for (int p = 0; p < blockEntity.getProcessCount(); p++) {
                MachineModuleBlockEntity.MachineProcess<?> process = blockEntity.getProcess(p);
                if(process.getInputStack(0) == stack) {
                    lines.add(process.getTimeLeftText());
                }
            }
        }
        super.appendHoverText(stack, world, lines, flag);
    }
}
