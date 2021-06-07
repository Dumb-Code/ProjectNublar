package net.dumbcode.projectnublar.server.tabs;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class TabHandler {

    public static final ItemGroup TAB = new ItemGroup(ProjectNublar.MODID) {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemHandler.AMBER.get());
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };
}
