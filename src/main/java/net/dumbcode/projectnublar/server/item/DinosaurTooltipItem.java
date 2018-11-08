package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class DinosaurTooltipItem extends BasicDinosaurItem {

    private final Function<ItemStack, List<String>> tooltipGetter;

    public DinosaurTooltipItem(Dinosaur dinosaur, Function<ItemStack, List<String>> tooltipGetter) {
        super(dinosaur);
        this.tooltipGetter = tooltipGetter;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.addAll(this.tooltipGetter.apply(stack));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
