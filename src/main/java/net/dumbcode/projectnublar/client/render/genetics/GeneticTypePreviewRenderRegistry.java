package net.dumbcode.projectnublar.client.render.genetics;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;
import net.dumbcode.dumblibrary.server.dna.GeneticType;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class GeneticTypePreviewRenderRegistry {
    private static final Map<GeneticType<?>, GeneticTypePreviewRender<?>> REGISTRY = new HashMap<>();
    private static final Default DEFAULT = new Default();

    public static <S extends GeneticFactoryStorage> void register(GeneticType<S> type, GeneticTypePreviewRender<S> render) {
        REGISTRY.put(type, render);
    }

    @SuppressWarnings("unchecked")
    public static <S extends GeneticFactoryStorage> GeneticTypePreviewRender<S> getType(EntityGeneticRegistry.Entry<S> entry) {
        GeneticTypePreviewRender<?> render = REGISTRY.get(entry.getType());
        if(render != null) {
            return (GeneticTypePreviewRender<S>) render;
        }
        return (GeneticTypePreviewRender<S>) DEFAULT;
    }

    private static class Default implements GeneticTypePreviewRender<GeneticFactoryStorage> {

        @Override
        public void render(MatrixStack stack, EntityGeneticRegistry.Entry<GeneticFactoryStorage> entry, int x, int y, int width, int height) {
            Minecraft.getInstance().font.draw(stack, "UNSET!!" + entry.getType().getRegistryName().toString() + "!!UNSET", x, y, -1);
        }
    }
}
