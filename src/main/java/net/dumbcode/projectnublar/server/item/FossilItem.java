package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class FossilItem extends Item implements DinosaurProvider {

    @Getter
    private final Dinosaur dinosaur;
    @Getter
    private final String varient;
    private final TextComponentTranslation stackDisplayName;

    public FossilItem(Dinosaur dinosaur, String varient) {
        this.dinosaur = dinosaur;
        this.varient = varient;
        this.stackDisplayName = new TextComponentTranslation(ProjectNublar.MODID+".item.fossil.name", this.dinosaur.createNameComponent(), this.varient);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return stackDisplayName.getUnformattedText();
    }
}
