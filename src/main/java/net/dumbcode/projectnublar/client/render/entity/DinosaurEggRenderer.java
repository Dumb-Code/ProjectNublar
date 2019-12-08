package net.dumbcode.projectnublar.client.render.entity;

import net.dumbcode.projectnublar.server.entity.DinosaurEggEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class DinosaurEggRenderer extends Render<DinosaurEggEntity> {

    public DinosaurEggRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.3F;
    }

    @Override
    public void doRender(DinosaurEggEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.enableBlend();
        bindTexture(this.getEntityTexture(entity));

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.translate(0, 1.5, 0);
        GlStateManager.scale(-1, -1, 1);

        GlStateManager.rotate(entity.randomRotation, 0, 1, 0);

        entity.getType().getEggModel().renderBoxes(1/16F);

        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(DinosaurEggEntity entity) {
        return entity.getType().getTexture();
    }
}
