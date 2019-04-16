package net.dumbcode.projectnublar.server.world.structures.structures.template.data;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;

public class DataHandlers {
    public static DataHandler LOOTTABLE = new DataHandler(DataHandler.Scope.BLOCK, s -> s.startsWith("chest~"),(world, pos, name, random) -> {
        TileEntity tileEntity = world.getTileEntity(pos.down());
        if(tileEntity instanceof TileEntityLockableLoot) {
            ResourceLocation res = new ResourceLocation(name.substring(6));
            ((TileEntityLockableLoot) tileEntity).setLootTable(new ResourceLocation(res.getResourceDomain(), "chests/" + res.getResourcePath()), random.nextLong());
        }
        return Blocks.AIR.getDefaultState();
    });
}
