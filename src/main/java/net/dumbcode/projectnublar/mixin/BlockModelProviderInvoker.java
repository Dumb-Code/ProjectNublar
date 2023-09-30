package net.dumbcode.projectnublar.mixin;

import net.minecraft.block.Block;
import net.minecraft.data.BlockModelProvider;
import net.minecraft.data.IFinishedBlockState;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockModelProvider.class)
public class BlockModelProviderInvoker {
    @Invoker("createFence")
    public static IFinishedBlockState createFence(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation1) {
        throw new AssertionError();
    }

    @Invoker("createFenceGate")
    public static IFinishedBlockState createFenceGate(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation1, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
        throw new AssertionError();
    }

    @Invoker("createStairs")
    public static IFinishedBlockState createStairs(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation1, ResourceLocation resourceLocation2) {
        throw new AssertionError();
    }

    @Invoker("createWall")
    public static IFinishedBlockState createWall(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation1, ResourceLocation resourceLocation2) {
        throw new AssertionError();
    }
}
