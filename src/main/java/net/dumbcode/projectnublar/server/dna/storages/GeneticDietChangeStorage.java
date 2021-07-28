package net.dumbcode.projectnublar.server.dna.storages;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.storages.RandomUUIDStorage;
import net.dumbcode.projectnublar.server.entity.ai.objects.FeedingDiet;
import net.minecraft.nbt.CompoundNBT;

public class GeneticDietChangeStorage extends RandomUUIDStorage {

    @Getter
    @Setter
    @Accessors(chain = true)
    private FeedingDiet diet = new FeedingDiet();

    @Override
    public CompoundNBT serialize(CompoundNBT nbt) {
        this.diet.writeToNBT(nbt);
        return super.serialize(nbt);
    }

    @Override
    public void deserialize(CompoundNBT nbt) {
        super.deserialize(nbt);
        this.diet.fromNBT(nbt);
    }

    @Override
    public void render(MatrixStack stack, EntityGeneticRegistry.Entry<?> entry, int x, int y, int height, int width) {
        //DO: `Can Consume: [...]`, where [...] is a stencil that scrolls
    }
}
