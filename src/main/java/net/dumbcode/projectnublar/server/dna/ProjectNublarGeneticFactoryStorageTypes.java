package net.dumbcode.projectnublar.server.dna;

import net.dumbcode.dumblibrary.server.dna.GeneticFactoryStorageType;
import net.dumbcode.dumblibrary.server.dna.storages.GeneticFieldModifierStorage;
import net.dumbcode.projectnublar.server.dna.storages.GeneticDietChangeStorage;

public class ProjectNublarGeneticFactoryStorageTypes {
    public static final GeneticFactoryStorageType<GeneticDietChangeStorage> DIET = new GeneticFactoryStorageType<GeneticDietChangeStorage>() {};
}
