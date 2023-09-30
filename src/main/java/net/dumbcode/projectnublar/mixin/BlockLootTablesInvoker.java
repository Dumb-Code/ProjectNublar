package net.dumbcode.projectnublar.mixin;

import net.minecraft.block.Block;
import net.minecraft.data.BlockModelProvider;
import net.minecraft.data.IFinishedBlockState;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockLootTables.class)
public class BlockLootTablesInvoker {
    @Invoker("createSlabItemTable")
    public static LootTable.Builder createSlabItemTable(Block block) {
        throw new AssertionError();
    }
}
