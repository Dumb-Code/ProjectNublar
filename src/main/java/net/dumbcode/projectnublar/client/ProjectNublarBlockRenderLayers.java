package net.dumbcode.projectnublar.client;

import static net.minecraft.client.renderer.RenderTypeLookup.setRenderLayer;
import static net.dumbcode.projectnublar.server.block.BlockHandler.*;

import net.dumbcode.dumblibrary.server.registry.RegistryMap;
import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.FossilBlock;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProjectNublarBlockRenderLayers {
    public static void setRenderLayers() {
        setRenderLayer(ELECTRIC_FENCE.get(), RenderType.solid());
        setRenderLayer(HIGH_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());
        setRenderLayer(LOW_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());

        setRenderLayers(UNBUILT_INCUBATOR.get(), RenderType.solid(), RenderType.cutout(), RenderType.translucent());

        for (RegistryMap<Dinosaur, FossilBlock> map : FOSSIL.values()) {
            for (FossilBlock block : map.values()) {
                setRenderLayers(block, RenderType.cutout());
            }
        }

    }

    private static void setRenderLayers(Block block, RenderType... types) {
        Set<RenderType> set = new HashSet<>(Arrays.asList(types));
        setRenderLayer(block, set::contains);
    }
}
