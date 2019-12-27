package net.dumbcode.projectnublar.server.tablet;

import net.minecraft.nbt.NBTTagCompound;

public interface TabletModuleStorage {
    void readFromNBT(NBTTagCompound compound);

    NBTTagCompound writeToNBT();

    class Empty implements TabletModuleStorage {

        @Override
        public void readFromNBT(NBTTagCompound compound) {
        }

        @Override
        public NBTTagCompound writeToNBT() {
            return new NBTTagCompound();
        }
    }
}
