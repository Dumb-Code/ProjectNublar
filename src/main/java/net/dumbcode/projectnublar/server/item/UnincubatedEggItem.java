package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
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
        lines.add(new StringTextComponent(MathUtils.ensureTrailingZeros(stack.getOrCreateTagElement(ProjectNublar.MODID).getFloat("AmountDone"), 1)));
        super.appendHoverText(stack, world, lines, flag);
    }
}
