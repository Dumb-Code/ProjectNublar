package net.dumbcode.projectnublar.client;

import static net.minecraft.client.renderer.RenderTypeLookup.setRenderLayer;
import static net.dumbcode.projectnublar.server.block.BlockHandler.*;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ProjectNublarBlockRenderLayers {
    public static void setRenderLayers(FMLClientSetupEvent event) {
        setRenderLayer(ELECTRIC_FENCE.get(), RenderType.solid());
        setRenderLayer(HIGH_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());
        setRenderLayer(LOW_SECURITY_ELECTRIC_FENCE_POLE.get(), RenderType.cutout());

    }
}
