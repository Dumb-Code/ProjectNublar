package net.dumbcode.projectnublar.server.dinosaur;

import lombok.Data;
import net.dumbcode.projectnublar.client.render.model.DinosaurModelContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

@Data
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {
    @GameRegistry.ObjectHolder(ProjectNublar.MODID + ":velociraptor")
    public static Dinosaur MISSING = null;

    private final ModelProperties modelProperties = new ModelProperties();

    private DinosaurModelContainer modelContainer;


    @Nonnull //A quick nonnull registry name. Usefull to prevent complier warnings
    public ResourceLocation getRegName() {
        if(this.getRegistryName() == null) {
            throw new RuntimeException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }

    @Override
    public int hashCode() { //Prevent Lombok from overriding this
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) { //Prevent Lombok from overriding this
        return super.equals(o);
    }
}
