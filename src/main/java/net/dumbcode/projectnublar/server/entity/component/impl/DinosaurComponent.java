package net.dumbcode.projectnublar.server.entity.component.impl;

import io.netty.buffer.ByteBuf;
import net.dumbcode.dumblibrary.server.entity.component.additionals.RenderLocationComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class DinosaurComponent implements RenderLocationComponent {

    public Dinosaur dinosaur = DinosaurHandler.TYRANNOSAURUS;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("dinosaur", this.dinosaur.getRegName().toString());
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
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

        texture.addFolderName("textures/entities", 0);
        texture.addFolderName(this.dinosaur.getRegName().getPath(), 10);

    }
}
