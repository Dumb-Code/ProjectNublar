package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FossilItem extends BasicDinosaurItem {

    @Getter
    private final String variant;
    private final TranslationTextComponent stackDisplayName;

    public FossilItem(Dinosaur dinosaur, String variant, Item.Properties properties) {
        super(dinosaur, properties);
        this.variant = variant;
        this.stackDisplayName = new TranslationTextComponent(ProjectNublar.MODID + ".item.fossil.name", this.dinosaur.createNameComponent(), this.variant);
    }

    @Override
    public ITextComponent getDescription() {
        return this.stackDisplayName;
    }
}
