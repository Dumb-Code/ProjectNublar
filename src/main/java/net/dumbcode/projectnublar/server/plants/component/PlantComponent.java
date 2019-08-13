package net.dumbcode.projectnublar.server.plants.component;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.FinalizableComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.plants.Plant;
import net.dumbcode.projectnublar.server.plants.PlantHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Objects;

public class PlantComponent implements EntityComponent, FinalizableComponent {

    @NonNull @Getter private Plant plant = PlantHandler.CYCAD;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("plant", Objects.requireNonNull(this.plant.getRegistryName()).toString());
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        ResourceLocation identifier = new ResourceLocation(compound.getString("plant"));
        if (ProjectNublar.PLANT_REGISTRY.containsKey(identifier)) {
            this.plant = ProjectNublar.PLANT_REGISTRY.getValue(identifier);
        } else {
            ProjectNublar.getLogger().warn("Parsed invalid plant component '{}'", identifier);
            this.plant = PlantHandler.CYCAD;
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, this.plant);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.plant = ByteBufUtils.readRegistryEntry(buf, ProjectNublar.PLANT_REGISTRY);
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
