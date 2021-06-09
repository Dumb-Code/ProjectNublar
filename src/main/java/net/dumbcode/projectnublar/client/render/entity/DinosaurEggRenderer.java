package net.dumbcode.projectnublar.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.server.entity.DinosaurEggEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class DinosaurEggRenderer extends EntityRenderer<DinosaurEggEntity> {

    public DinosaurEggRenderer(EntityRendererManager renderManager) {
        super(renderManager);
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(DinosaurEggEntity entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        super.render(entity, entityYaw, partialTicks, stack, buffers, light);

        float scale = entity.getRandomScaleAdjustment();
        this.shadowRadius = scale * 0.3F;

        stack.pushPose();
        stack.scale(scale, scale, scale);
        stack.translate(0, -1.5, 0);

        stack.mulPose(Vector3f.YP.rotationDegrees(entity.getRandomRotation()));

        entity.getEggType().getEggModel().renderBoxes(stack, light, buffers, this.getTextureLocation(entity));
        stack.popPose();
    }


    @Override
    public ResourceLocation getTextureLocation(DinosaurEggEntity entity) {
        return entity.getEggType().getTexture();
    }


}
