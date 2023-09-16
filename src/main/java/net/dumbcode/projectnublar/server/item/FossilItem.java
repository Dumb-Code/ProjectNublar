package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

/**
 * MARKED FOR REMOVAL
 * DO NOT USE
 */
//TODO: Remove
@Deprecated
public class FossilItem extends BasicDinosaurItem {

    @Getter
    private final String variant;

    public FossilItem(Dinosaur dinosaur, String translationKey, String variant, Properties properties) {
        super(dinosaur, translationKey, properties);
        this.variant = variant;
    }

    @Override
    protected void addTranslation(Consumer<Object> consumer) {
        super.addTranslation(consumer);
        consumer.accept(ProjectNublar.translate("item.fossil.type." + this.variant));
    }
}
