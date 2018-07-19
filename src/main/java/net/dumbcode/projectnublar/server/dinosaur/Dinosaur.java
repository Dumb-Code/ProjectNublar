package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.dumbcode.projectnublar.client.render.dinosaur.PoseHandler;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.GrowthStage;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.List;

@Data
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {

    @GameRegistry.ObjectHolder(ProjectNublar.MODID + ":velociraptor")
    public static Dinosaur MISSING = null;

    List<GrowthStage> modelGrowthStages = Lists.newArrayList(GrowthStage.ADULT);

//    @Setter(AccessLevel.NONE)
    private PoseHandler poseHandler;

    private TabulaModel model; //TODO: remove

    @Nonnull
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
