package net.dumbcode.projectnublar.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.EntityPart;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class EntityPartRenderer extends EntityRenderer<EntityPart> {

    public EntityPartRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }


    @Override
    public void render(EntityPart entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light) {
        super.render(entity, entityYaw, partialTicks, stack, buffers, light);
        if (ProjectNublar.DEBUG) {
            stack.pushPose();
            stack.translate(
                -MathHelper.lerp(partialTicks, entity.xOld, entity.position().x),
                -MathHelper.lerp(partialTicks, entity.yOld, entity.position().y),
                -MathHelper.lerp(partialTicks, entity.zOld, entity.position().z)
            );
            WorldRenderer.renderLineBox(stack, buffers.getBuffer(RenderType.lines()), entity.getBoundingBox(), 1, 1, 1, 1);
            stack.popPose();
            renderNameTag(entity, new StringTextComponent(entity.getPartName()), stack, buffers, light);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityPart p_110775_1_) {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }
}
