package net.dumbcode.projectnublar.client.render.genetics;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorage;

public interface GeneticTypePreviewRender<S extends GeneticFactoryStorage> {
    void render(MatrixStack stack, EntityGeneticRegistry.Entry<S> entry, int x, int y, int width, int height);


}
