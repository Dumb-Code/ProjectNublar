package net.dumbcode.projectnublar.server.item;

import net.dumbcode.dumblibrary.server.dna.GeneticEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class DnaHoverDinosaurItem extends BasicDinosaurItem {
    public DnaHoverDinosaurItem(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(dinosaur, translationKey, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.appendHoverText(stack, world, list, flag);
        if(Screen.hasShiftDown()) {
            CompoundNBT tag = stack.getOrCreateTagElement(ProjectNublar.MODID);

            tag.getList("Genetics", Constants.NBT.TAG_COMPOUND).stream()
                .map(g -> GeneticEntry.deserialize((CompoundNBT) g))
                .map(GeneticEntry::gatherTextComponents)
                .forEach(list::add);

            Object arg;
            if(tag.contains("IsMale", Constants.NBT.TAG_BYTE)) {
                if(tag.getBoolean("IsMale")) {
                    arg = ProjectNublar.translate("gender.type.male");
                } else {
                    arg = ProjectNublar.translate("gender.type.female");
                }

            } else {
                arg = ProjectNublar.translate("gender.type.random");
            }
            list.add(ProjectNublar.translate("gender.title", arg));

        } else {
            list.add(ProjectNublar.translate("item.test_tube.shift_for_info").withStyle(TextFormatting.AQUA));
        }

    }

    public static void copyDataNBT(ItemStack from, ItemStack to) {
        CompoundNBT inTag = from.getOrCreateTagElement(ProjectNublar.MODID);
        CompoundNBT outTag = to.getOrCreateTagElement(ProjectNublar.MODID);

        outTag.put("Genetics", inTag.getList("Genetics", Constants.NBT.TAG_COMPOUND).copy());
        if(inTag.contains("IsMale", Constants.NBT.TAG_BYTE)) {
            outTag.putBoolean("IsMale", inTag.getBoolean("IsMale"));
        }

    }
}
