package net.dumbcode.projectnublar.mixin;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public interface SimpleBakedModelAccessor {
    @Accessor
    List<BakedQuad> getUnculledFaces();

    @Accessor
    Map<Direction, List<BakedQuad>> getCulledFaces();
}
