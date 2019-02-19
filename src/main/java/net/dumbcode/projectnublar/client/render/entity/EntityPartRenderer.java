package net.dumbcode.projectnublar.client.render.entity;

import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class EntityPartRenderer extends Render<EntityPart> {

    public static final boolean debug = false;

    public EntityPartRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPart entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if(debug) {
            GlStateManager.pushMatrix();
            renderOffsetAABB(entity.getEntityBoundingBox(), x - entity.lastTickPosX, y - entity.lastTickPosY, z - entity.lastTickPosZ);
            GlStateManager.popMatrix();
            this.renderLivingLabel(entity, entity.getPartName(), x, y, z, 64);

        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPart entity) {
        return null;
    }
}
