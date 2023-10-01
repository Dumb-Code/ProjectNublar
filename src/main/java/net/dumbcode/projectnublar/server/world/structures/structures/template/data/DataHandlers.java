package net.dumbcode.projectnublar.server.world.structures.structures.template.data;

import net.minecraft.block.Blocks;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.resources.ResourceLocation;

public class DataHandlers {
    public static DataHandler LOOTTABLE = new DataHandler(DataHandler.Scope.BLOCK, s -> s.startsWith("chest~"), (world, pos, name, random, decision) -> {
        TileEntity tileEntity = world.getBlockEntity(pos.below());
        if(tileEntity instanceof LockableLootTileEntity) {
            ResourceLocation res = new ResourceLocation(name.substring(6));
            ((LockableLootTileEntity) tileEntity).setLootTable(new ResourceLocation(res.getNamespace(), "chests/" + res.getPath()), random.nextLong());
        }
        return Blocks.AIR.defaultBlockState();
    });
}
