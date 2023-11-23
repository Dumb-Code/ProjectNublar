package net.dumbcode.projectnublar.server.plants.component;

import lombok.Getter;
import lombok.NonNull;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.plants.PlantHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class PlantComponent extends EntityComponent implements FinalizableComponent {

    @NonNull @Getter private Plant plant = PlantHandler.CYCAD.get();

    @Override
    public CompoundTag serialize(CompoundTag compound) {
        compound.putString("plant", Objects.requireNonNull(this.plant.getRegistryName()).toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        super.deserialize(compound);
        ResourceLocation identifier = new ResourceLocation(compound.getString("plant"));
        if (PlantHandler.getRegistry().containsKey(identifier)) {
            this.plant = PlantHandler.getRegistry().getValue(identifier);
        } else {
            ProjectNublar.LOGGER.warn("Parsed invalid plant component '{}'", identifier);
            this.plant = PlantHandler.CYCAD.get();
        }
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeRegistryId(this.plant);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf) {
        this.plant = buf.readRegistryIdSafe(Plant.class);
    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        // *to the theme of pink panther*
        //todo
        //todo
        //todo todo todo todo todoooooooooooooooooo
        //todotodotodo
    }
}
