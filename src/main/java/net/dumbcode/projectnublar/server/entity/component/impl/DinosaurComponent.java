package net.dumbcode.projectnublar.server.entity.component.impl;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class DinosaurComponent implements EntityComponent {
    private EntityComponentMap map;
    @Getter private Dinosaur dinosaur = Dinosaur.MISSING;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setString("dinosaur", this.dinosaur.getRegName().toString());
        return compound;
    }

    public void setDinosaur(Dinosaur dinosaur) {
        (this.dinosaur = dinosaur).setProperties(this.map);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        ResourceLocation identifier = new ResourceLocation(compound.getString("dinosaur"));
        if (ProjectNublar.DINOSAUR_REGISTRY.containsKey(identifier)) {
            this.setDinosaur(ProjectNublar.DINOSAUR_REGISTRY.getValue(identifier));
        } else {
            ProjectNublar.getLogger().warn("Parsed invalid dinosaur component '{}'", identifier);
            this.setDinosaur(Dinosaur.MISSING);
        }
    }

    @Override
    public void onAdded(EntityComponentMap map) {
        this.map = map;
    }

    @Override
    public void serialize(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, this.dinosaur);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.setDinosaur(ByteBufUtils.readRegistryEntry(buf, ProjectNublar.DINOSAUR_REGISTRY));
    }
}
