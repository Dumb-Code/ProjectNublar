package net.dumbcode.projectnublar.server.item;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

public interface DinosaurStack extends StackModelVarient<Dinosaur> {

    @Override
    default IForgeRegistry<Dinosaur> getRegistry() {
        return ProjectNublar.DINOSAUR_REGISTRY;
    }

    @Override
    default String getKey() {
        return "dinosaur";
    }

    @Nonnull
    static DinosaurStack getFromStack(ItemStack stack) {
        Item item = stack.getItem();
        if(item instanceof DinosaurStack) {
            return (DinosaurStack)item;
        } else if((item instanceof ItemBlock && ((ItemBlock)item).getBlock() instanceof DinosaurStack)) {
            return (DinosaurStack)((ItemBlock)item).getBlock();
        } else {
            return MISSING_PROVIDER;
        }
    }

    default boolean isMissing() {
        return this == MISSING_PROVIDER;
    }

    DinosaurStack MISSING_PROVIDER = new DinosaurStack() {
        @Override
        public ItemStack getItemStack(Dinosaur dinosaur) {
            return ItemStack.EMPTY;
        }

        @Override
        public Dinosaur getValue(ItemStack stack) {
            return Dinosaur.MISSING;
        }
    };

}
