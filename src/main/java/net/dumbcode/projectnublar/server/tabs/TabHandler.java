package net.dumbcode.projectnublar.server.tabs;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class TabHandler {

    public static final ItemGroup TAB = new ItemGroup(ProjectNublar.MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ItemBlock.getItemFromBlock(Blocks.DEADBUSH)); //TODO: custom item
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };
}
