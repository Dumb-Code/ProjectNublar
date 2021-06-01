package net.dumbcode.projectnublar.server.block;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

public class DinosaurBlockProperty extends Property<Dinosaur> {
    protected DinosaurBlockProperty(String name) {
        super(name, Dinosaur.class);
    }

    @Override
    public Collection<Dinosaur> getPossibleValues() {
        System.out.println(ProjectNublar.DINOSAUR_REGISTRY.getValues());
        return ProjectNublar.DINOSAUR_REGISTRY.getValues();
    }

    @Override
    public String getName(Dinosaur dinosaur) {
        ResourceLocation name = dinosaur.getRegistryName();
        return name.getNamespace() + "_" + name.getPath();
    }

    @Override
    public Optional<Dinosaur> getValue(String name) {
        return Optional.ofNullable(ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(name.replaceFirst("_", ":"))));
    }

    public static DinosaurBlockProperty of(String name) {
        return new DinosaurBlockProperty(name);
    }

}
