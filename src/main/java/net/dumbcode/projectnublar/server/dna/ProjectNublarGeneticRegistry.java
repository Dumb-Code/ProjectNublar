package net.dumbcode.projectnublar.server.dna;

import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.dumblibrary.server.dna.GeneticTypes;
import net.dumbcode.projectnublar.server.dna.storages.GeneticDietChangeStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;

public class ProjectNublarGeneticRegistry {
    public static void register() {
        EntityGeneticRegistry r = EntityGeneticRegistry.INSTANCE;

        //Polar Bear: Increase strength, defense
        r.register(EntityType.POLAR_BEAR, GeneticHandler.STRENGTH.get(), 0.5F);
        r.register(EntityType.POLAR_BEAR, GeneticHandler.DEFENSE.get(), 0.75F);

        //Panda: Increase strength, reduce defense, Decrease in eat rate, Increase in stomach capacity
        r.register(EntityType.PANDA, GeneticHandler.STRENGTH.get(), 0.25F);
        r.register(EntityType.PANDA, GeneticHandler.DEFENSE.get(), -0.5F);
        r.register(EntityType.PANDA, GeneticHandler.EAT_RATE.get(), -0.1F);
        r.register(EntityType.PANDA, GeneticHandler.STOMACH_CAPACITY.get(), 0.75F);

        //Turtle: Increase defense
        r.register(EntityType.TURTLE, GeneticHandler.DEFENSE.get(), 0.5F);

        //Wolf: Increase Max Pack size of carnivores
        r.register(EntityType.WOLF, GeneticHandler.HUNTING_PACK_SIZE.get(), 0.75F);

        //Hoglin: Increase strength & defense, Increased aggression
        r.register(EntityType.HOGLIN, GeneticHandler.STRENGTH.get(), 0.5F);
        r.register(EntityType.HOGLIN, GeneticHandler.DEFENSE.get(), 0.3F);
        r.register(EntityType.HOGLIN, GeneticHandler.AGGRESSION.get(), 0.85F);

        //Sheep: Increase in herd size for herbivores, can eat grass
        r.register(EntityType.SHEEP, GeneticHandler.HERD_SIZE.get(), 0.5F);
        r.register(EntityType.SHEEP, GeneticHandler.DIET_CHANGE.get(), 0F)
            .getDiet().add(50, 5, Blocks.TALL_GRASS);

        //Cow: Increase in herd size for herbivores, can eat grass - herbivores
        r.register(EntityType.COW, GeneticHandler.HERD_SIZE.get(), 0.3F);
        r.register(EntityType.COW, GeneticHandler.DIET_CHANGE.get(), 0F)
            .getDiet().add(50, 5, Blocks.TALL_GRASS);

        //Pig: Increased defense
        r.register(EntityType.PIG, GeneticHandler.DEFENSE.get(), 0.2F);

        //Llama: Increased defense, Increase in hunger
        r.register(EntityType.LLAMA, GeneticHandler.DEFENSE.get(), 0.25F);
        r.register(EntityType.LLAMA, GeneticHandler.STOMACH_CAPACITY.get(), 0.3F);

        //Donkey: Increase in strength
        r.register(EntityType.DONKEY, GeneticHandler.STRENGTH.get(), 0.25F);

        //Mooshroom: Ability to eat mushrooms for herbivores
        r.register(EntityType.MOOSHROOM, GeneticHandler.DIET_CHANGE.get(), 0F).getDiet()
            .add(15, 15, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)
            .add(150, 150, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM);


    }
}
