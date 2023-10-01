package net.dumbcode.projectnublar.server.entity.component.impl;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.DinosaurInformation;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class DinosaurComponent extends EntityComponent implements RenderLocationComponent, FinalizableComponent, CanBreedComponent, TrackingDataComponent {

    private Dinosaur dinosaur = DinosaurHandler.TYRANNOSAURUS.get();

    @Override
    public CompoundNBT serialize(CompoundNBT compound) {
        compound.putString("dinosaur", this.dinosaur.getRegName().toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundNBT compound) {
        super.deserialize(compound);
        ResourceLocation identifier = new ResourceLocation(compound.getString("dinosaur"));
        if (DinosaurHandler.getRegistry().containsKey(identifier)) {
            this.dinosaur = DinosaurHandler.getRegistry().getValue(identifier);
        } else {
            ProjectNublar.LOGGER.warn("Parsed invalid dinosaur component '{}'", identifier);
            this.dinosaur = DinosaurHandler.TYRANNOSAURUS.get();
        }
    }

    @Override
    public void serialize(PacketBuffer buf) {
        buf.writeRegistryId(this.dinosaur);
    }

    @Override
    public void deserialize(PacketBuffer buf) {
        this.dinosaur = buf.readRegistryIdSafe(Dinosaur.class);
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        texture.setModid(this.dinosaur.getRegName().getNamespace());
        fileLocation.setModid(this.dinosaur.getRegName().getNamespace());

        fileLocation.addFolderName("models/entities", 0);
        fileLocation.addName(this.dinosaur.getRegName().getPath(), 10);

        texture.addFolderName("textures/entities/" + this.dinosaur.getRegName().getPath(), 0);
        texture.addFileName(this.dinosaur.getRegName().getPath(), 0);

    }

    @Override
    public void finalizeComponent(ComponentAccess entity) {
        entity.get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
            String name = entity.get(ComponentHandler.AGE).flatMap(AgeComponent::getModelState).orElse(AgeStage.MISSING).getName();
            System.out.println(name);
            c.setAnimationContainer(this.dinosaur.getModelContainer().get(name));
        });
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return otherEntity.get(ComponentHandler.DINOSAUR).map(d -> d.dinosaur == this.dinosaur).orElse(false);
    }

    @Override
    public void addTrackingData(ComponentAccess entity, Consumer<Supplier<TrackingDataInformation>> consumer) {
        consumer.accept(() -> new DinosaurInformation(this.dinosaur));
    }
}
