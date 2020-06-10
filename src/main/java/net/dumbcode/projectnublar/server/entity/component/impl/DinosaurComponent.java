package net.dumbcode.projectnublar.server.entity.component.impl;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.animation.objects.AnimationEntry;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentTypes;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.AnimationFactorHandler;
import net.dumbcode.projectnublar.server.animation.AnimationHandler;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.additionals.TrackingDataComponent;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.info.DinosaurInformation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class DinosaurComponent extends EntityComponent implements RenderLocationComponent, FinalizableComponent, CanBreedComponent, TrackingDataComponent {

    private Dinosaur dinosaur = DinosaurHandler.TYRANNOSAURUS;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("dinosaur", this.dinosaur.getRegName().toString());
        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        super.deserialize(compound);
        ResourceLocation identifier = new ResourceLocation(compound.getString("dinosaur"));
        if (ProjectNublar.DINOSAUR_REGISTRY.containsKey(identifier)) {
            this.dinosaur = ProjectNublar.DINOSAUR_REGISTRY.getValue(identifier);
        } else {
            ProjectNublar.getLogger().warn("Parsed invalid dinosaur component '{}'", identifier);
            this.dinosaur = DinosaurHandler.TYRANNOSAURUS;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, this.dinosaur);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.dinosaur = ByteBufUtils.readRegistryEntry(buf, ProjectNublar.DINOSAUR_REGISTRY);
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
        entity.get(EntityComponentTypes.ANIMATION).ifPresent(c ->
            c.setAnimationContainer(this.dinosaur.getModelContainer().get(entity.get(ComponentHandler.AGE).flatMap(AgeComponent::getModelState).orElse(AgeStage.MISSING).getName()))
        );
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
