package net.dumbcode.projectnublar.client.model.fossil;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

import java.util.Random;
import java.util.concurrent.Callable;

import static net.minecraft.inventory.container.PlayerContainer.BLOCK_ATLAS;

public class FossilItemRenderer extends ItemStackTileEntityRenderer implements Callable<ItemStackTileEntityRenderer> {

    public static final FossilItemRenderer INSTANCE = new FossilItemRenderer();
    @Override
    public void renderByItem(ItemStack pStack, ItemCameraTransforms.TransformType pTransformType, MatrixStack pPoseStack, IRenderTypeBuffer pBuffer, int pPackedLight, int pPackedOverlay) {
        IBakedModel model = Minecraft.getInstance().getItemRenderer().getModel(pStack, null, null);
        if (pStack.getItem() instanceof FossilItem && model instanceof FossilBakedModel) {
            pPoseStack.pushPose();
            BufferBuilder builder = (BufferBuilder) pBuffer.getBuffer(RenderType.itemEntityTranslucentCull(BLOCK_ATLAS));
            for (BakedQuad quad : model.getQuads(null, null, new Random())) {
                if (quad.getTintIndex() == 2) {
                    float r = (float)(((FossilBakedModel) model).tint >> 16 & 255) / 255.0F;
                    float g = (float)(((FossilBakedModel) model).tint >> 8 & 255) / 255.0F;
                    float b = (float)(((FossilBakedModel) model).tint & 255) / 255.0F;
                    builder.putBulkData(pPoseStack.last(), quad, r, g, b, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
                } else {
                    builder.putBulkData(pPoseStack.last(), quad, 1, 1, 1, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);
            }
        }
            render(builder);
            pPoseStack.popPose();
        }
        super.renderByItem(pStack, pTransformType, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void render(BufferBuilder buffer) {
        RenderType.entityTranslucent(BLOCK_ATLAS).setupRenderState();
        RenderSystem.enableCull();
        buffer.end();
        WorldVertexBufferUploader.end(buffer);
        RenderType.entityTranslucent(BLOCK_ATLAS).clearRenderState();
    }

    @Override
    public FossilItemRenderer call() throws Exception {
        return INSTANCE;
    }
}
