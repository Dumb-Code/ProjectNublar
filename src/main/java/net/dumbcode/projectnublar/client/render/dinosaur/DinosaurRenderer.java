package net.dumbcode.projectnublar.client.render.dinosaur;

import net.dumbcode.projectnublar.client.render.model.ModelMissing;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Locale;

public class DinosaurRenderer extends RenderLiving<DinosaurEntity> {

    public DinosaurRenderer(RenderManager renderManagerIn) {
        super(renderManagerIn, null, 1f);
    }

    @Override
    public void doRender(DinosaurEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.mainModel = entity.getDinosaur().getModelContainer().getModelMap().get(entity.getGrowthStage());
        if(this.mainModel == null) {
            this.mainModel = ModelMissing.INSTANCE;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(DinosaurEntity entity) {
        ResourceLocation regname = entity.getDinosaur().getRegName();
        return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (entity.isMale() ? "male" : "female") + "_" + entity.getGrowthStage().name().toLowerCase(Locale.ROOT) + ".png");
    }
}
