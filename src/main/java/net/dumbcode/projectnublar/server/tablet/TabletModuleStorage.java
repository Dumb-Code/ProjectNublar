package net.dumbcode.projectnublar.server.tablet;

import net.minecraft.nbt.CompoundNBT;

public interface TabletModuleStorage {
    void readFromNBT(CompoundNBT compound);

    CompoundNBT writeToNBT();

    class Empty implements TabletModuleStorage {

        @Override
        public void readFromNBT(CompoundNBT compound) {
        }

        @Override
        public CompoundNBT writeToNBT() {
            return new CompoundNBT();
        }
    }
}
