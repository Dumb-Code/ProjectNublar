package net.dumbcode.projectnublar.client;

import static net.minecraft.client.renderer.RenderTypeLookup.setRenderLayer;
import static net.dumbcode.projectnublar.server.block.BlockHandler.*;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProjectNublarBlockRenderLayers {
    public static void setRenderLayers() {
        setRenderLayer(ELECTRIC_FENCE.get(), RenderType.solid());
        setRenderLayer(HIGH_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());
        setRenderLayer(LOW_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());

        setRenderLayers(UNBUILT_INCUBATOR.get(), RenderType.solid(), RenderType.cutout(), RenderType.translucent());

        setRenderLayers(FOSSIL_BLOCK.get(), RenderType.translucent(), RenderType.solid());
    }

    private static void setRenderLayers(Block block, RenderType... types) {
        Set<RenderType> set = new HashSet<>(Arrays.asList(types));
        setRenderLayer(block, set::contains);
    }
}
