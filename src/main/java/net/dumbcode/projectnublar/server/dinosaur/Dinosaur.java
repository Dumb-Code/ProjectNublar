package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.entity.EntityAnimatable;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.*;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {
    @GameRegistry.ObjectHolder(ProjectNublar.MODID + ":missing")
    public static Dinosaur MISSING = null;

    private final ModelProperties modelProperties = new ModelProperties();
    private final ItemProperties itemProperties = new ItemProperties();
    private final EntityProperties entityProperties = new EntityProperties();
    private final SkeletalInfomation skeletalInfomation = new SkeletalInfomation();

    private ModelContainer modelContainer;
    private ModelContainer noAnimationModelContainer;

    public Dinosaur() {

    }

    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getResourcePath());
    }

    public DinosaurEntity createEntity(World world) {
        return this.getEntityProperties().getEntityCreateFunction().apply(world).setDinosaur(this);
    }

    @Nonnull //A quick nonnull registry name. Usefull to prevent complier warnings
    public ResourceLocation getRegName() {
        if(this.getRegistryName() == null) {
            throw new RuntimeException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }

    public ResourceLocation getTextureLocation(DinosaurEntity entity) {
        ResourceLocation regname = getRegName();
        return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (entity.isMale() ? "male" : "female") + "_" + entity.getGrowthStage().name().toLowerCase(Locale.ROOT) + ".png");
    }
}
