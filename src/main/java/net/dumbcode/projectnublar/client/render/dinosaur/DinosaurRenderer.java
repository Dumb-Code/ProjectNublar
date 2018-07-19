package net.dumbcode.projectnublar.client.render.dinosaur;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class DinosaurRenderer extends RenderLiving<DinosaurEntity> {


    public DinosaurRenderer(RenderManager renderManagerIn) {
        super(renderManagerIn, null, 1f);
    }

    @Override
    public void doRender(DinosaurEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.mainModel = entity.getDinosaur().getModel();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(DinosaurEntity entity) {
        return new ResourceLocation(ProjectNublar.MODID, "textures/entities/velociraptor/velociraptor_male_adult.png");
    }
}
