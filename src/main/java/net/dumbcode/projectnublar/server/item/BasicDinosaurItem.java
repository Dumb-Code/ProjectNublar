package net.dumbcode.projectnublar.server.item;

import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BasicDinosaurItem extends Item implements DinosaurProvider {
    @Getter protected final Dinosaur dinosaur;
    private final String translationKey;

    public BasicDinosaurItem(Dinosaur dinosaur, String translationKey, Properties properties) {
        super(properties);
        this.dinosaur = dinosaur;
        this.translationKey = translationKey;
    }

    @Override
    public ITextComponent getDescription() {
        return this.createTranslation();
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return this.createTranslation();
    }

    public ITextComponent createTranslation() {
        List<Object> list = new ArrayList<>();
        this.addTranslation(list::add);
        return ProjectNublar.translate("item."+this.translationKey+".name", list.toArray());
    }

    protected void addTranslation(Consumer<Object> consumer) {
        consumer.accept(this.dinosaur.createNameComponent());
    }

}
