package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class DriveItem extends Item {

    @Getter
    private final boolean ssd;

    public DriveItem(boolean ssd, Properties properties) {
        super(properties);
        this.ssd = ssd;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        for (DriveUtils.DriveEntry entry : DriveUtils.getAll(stack)) {
            tooltip.add(new TranslationTextComponent(entry.getName()).append(": " + entry.getAmount()));
        }
        super.appendHoverText(stack, world, tooltip, flagIn);
    }

}
