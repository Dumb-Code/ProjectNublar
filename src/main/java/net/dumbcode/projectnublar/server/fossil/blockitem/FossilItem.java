package net.dumbcode.projectnublar.server.fossil.blockitem;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.base.Fossil;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.text.WordUtils;

//TODO: tint the item model fossil overlay
public class FossilItem extends Item {
    public FossilItem(Properties properties) {
        super(properties);
    }

    @Override
    public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
        if (this.allowdedIn(pGroup)) {
            for (Fossil fossil : FossilHandler.FOSSIL_REGISTRY.get()) {
                pItems.add(stackWithFossil(fossil));
            }
        }
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        Fossil fossil = getFossil(stack);
        return Component.translatable("projectnublar.fossil", "", WordUtils.capitalizeFully(fossil.name));
    }

    public static Fossil getFossil(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
        return FossilHandler.FOSSIL_REGISTRY.get().getValue(new ResourceLocation(nbt.getString("fossil")));
    }

    public static ItemStack stackWithFossil(Fossil fossil) {
        ItemStack stack = new ItemStack(ItemHandler.FOSSIL_ITEM.get());
        CompoundTag nbt = stack.getOrCreateTagElement(ProjectNublar.MODID);
        nbt.putString("fossil", fossil.getRegistryName().toString());
        return stack;
    }
}
