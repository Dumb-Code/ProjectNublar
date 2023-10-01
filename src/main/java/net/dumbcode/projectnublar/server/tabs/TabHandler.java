package net.dumbcode.projectnublar.server.tabs;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.fossil.FossilHandler;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.item.ItemStack;

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

    public static final ItemGroup FOSSIL_TAB = new ItemGroup(ProjectNublar.MODID + ".fossils") {
        @Override
        public ItemStack makeIcon() {
            return FossilItem.stackWithFossil(FossilHandler.AMMONITE.get());
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    };

}
