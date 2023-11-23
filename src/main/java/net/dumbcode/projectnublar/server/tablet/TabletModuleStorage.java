package net.dumbcode.projectnublar.server.tablet;

import net.minecraft.nbt.CompoundTag;

public interface TabletModuleStorage {
    void readFromNBT(CompoundTag compound);

    CompoundTag writeToNBT();

    class Empty implements TabletModuleStorage {

        @Override
        public void readFromNBT(CompoundTag compound) {
        }

        @Override
        public CompoundTag writeToNBT() {
            return new CompoundTag();
        }
    }
}
