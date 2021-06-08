package net.dumbcode.projectnublar.server.entity.tracking.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.RepeatingIconDisplay;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
public class MetabolismInformation extends TooltipInformation {

    public static final String KEY = "metabolism_info";

    private static final int PADDING_AFTER_TEXT = 10;

    private final float food;
    private final float maxFood;
    private final RepeatingIconDisplay foodDisplay;

    private final float water;
    private final float maxWater;
    private final RepeatingIconDisplay waterDisplay;


    public MetabolismInformation(float food, float maxFood, float water, float maxWater) {
        this.food = food;
        this.maxFood = maxFood;
        this.foodDisplay = new RepeatingIconDisplay(this.food, this.maxFood, 9, 5, this.maxFood / 5F, this::renderFoodIcon);

        this.water = water;
        this.maxWater = maxWater;
        this.waterDisplay = new RepeatingIconDisplay(this.water, this.maxWater, 10, 4, this.maxWater / 4F, this::renderWaterIcon);

    }

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Nonnull
    @Override
    public Dimension getInfoDimensions() {
        Dimension d = super.getInfoDimensions();
        return new Dimension(d.width + PADDING_AFTER_TEXT + Math.max(this.foodDisplay.getWidth(), this.waterDisplay.getWidth()), Math.max(d.height, 27));
    }

    private void renderFoodIcon(MatrixStack stack, int x, int y, float size) {
        RenderUtils.draw256Texture(stack, x, y, 16, 27, 9, 9);
        if(size > 0) {
            RenderUtils.draw256Texture(stack, x, y, 52, 27, (int) (9F * size), 9);
        }
    }

    private void renderWaterIcon(MatrixStack stack, int x, int y, float size) {
        Function<ResourceLocation, TextureAtlasSprite> textureAtlas = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS);
        TextureAtlasSprite bottle = textureAtlas.apply(new ResourceLocation("items/potion_bottle_drinkable"));
        TextureAtlasSprite water = textureAtlas.apply(new ResourceLocation("items/potion_overlay"));

        RenderUtils.drawTextureAtlasSprite(stack, x, y, bottle, 11, 16D, 4D, 0D, 15D, 16D);
        if(size > 0) {
            float endY = 6 + 6 * (1 - size);
            RenderSystem.color4f(66F / 255F, 135 / 255F, 245 / 255F, 1F);
            RenderUtils.drawTextureAtlasSprite(stack, x, y + endY, water, 11, 16 - endY, 4, endY, 15, 16);
            RenderSystem.color4f(1F, 1F, 1F, 1F);
        }
    }

    @Override
    public void renderInfo(MatrixStack stack, int x, int y, int relativeMouseX, int relativeMouseY) {
        super.renderInfo(stack, x, y, relativeMouseX, relativeMouseY);
        Dimension dimension = super.getInfoDimensions();
        int startX = dimension.width + PADDING_AFTER_TEXT;

        Minecraft.getInstance().textureManager.bind(AbstractGui.GUI_ICONS_LOCATION);
        this.foodDisplay.render(stack, startX, y + 2);

        Minecraft.getInstance().textureManager.bind(PlayerContainer.BLOCK_ATLAS);
        this.waterDisplay.render(stack, startX + 3, y + 10);
    }

    @Override
    protected List<String> getTooltipLines() {
        return Arrays.asList(
            I18n.get("projectnublar.gui.tracking.metatlism.food"),
            I18n.get("projectnublar.gui.tracking.metatlism.water")
        );
    }

    public static void encodeNBT(CompoundNBT nbt, MetabolismInformation info) {
        nbt.putFloat("food", info.food);
        nbt.putFloat("max_food", info.maxFood);

        nbt.putFloat("water", info.water);
        nbt.putFloat("max_water", info.maxWater);
    }

    public static MetabolismInformation decodeNBT(CompoundNBT nbt) {
        return new MetabolismInformation(
            nbt.getFloat("food"), nbt.getFloat("max_food"),
            nbt.getFloat("water"), nbt.getFloat("max_water")
        );
    }

    public static void encodeBuf(PacketBuffer buf, MetabolismInformation info) {
        buf.writeFloat(info.food);
        buf.writeFloat(info.maxFood);
        buf.writeFloat(info.water);
        buf.writeFloat(info.maxWater);
    }

    public static MetabolismInformation decodeBuf(PacketBuffer buf) {
        return new MetabolismInformation(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }
}
