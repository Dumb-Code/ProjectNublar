package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.RepeatingIconDisplay;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
public class MetabolismInformation extends TooltipInformation {

    public static final String KEY = "metabolism_info";

    private static final int PADDING_AFTER_TEXT = 10;

    private final int food;
    private final int maxFood;
    private final RepeatingIconDisplay foodDisplay;

    private final int water;
    private final int maxWater;
    private final RepeatingIconDisplay waterDisplay;

    private final TextureAtlasSprite BOTTLE_SPRITE = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/potion_bottle_drinkable");
    private final TextureAtlasSprite BOTTLE_WATER_SPRITE = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/potion_overlay");

    public MetabolismInformation(int food, int maxFood, int water, int maxWater) {
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

    private void renderFoodIcon(int x, int y, float size) {
        RenderUtils.draw256Texture(x, y, 16, 27, 9, 9);
        if(size > 0) {
            RenderUtils.draw256Texture(x, y, 52, 27, 9F * size, 9);
        }
    }

    private void renderWaterIcon(int x, int y, float size) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        RenderUtils.drawTextureAtlasSprite(x, y, BOTTLE_SPRITE, 11, 16D, 4D, 0D, 15D, 16D);
        if(size > 0) {
            float endY = 6 + 6 * (1 - size);
            GlStateManager.color(66F / 255F, 135 / 255F, 245 / 255F, 1F);
            RenderUtils.drawTextureAtlasSprite(x, y + endY, BOTTLE_WATER_SPRITE, 11, 16 - endY, 4, endY, 15, 16);

        }
    }

    @Override
    public void renderInfo(int x, int y, int relativeMouseX, int relativeMouseY) {
        super.renderInfo(x, y, relativeMouseX, relativeMouseY);
        Dimension dimension = super.getInfoDimensions();
        int startX = dimension.width + PADDING_AFTER_TEXT;

        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
        this.foodDisplay.render(startX, y + 2);

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        this.waterDisplay.render(startX + 3, y + 10);
    }

    @Override
    protected List<String> getTooltipLines() {
        return Arrays.asList(
            I18n.format("projectnublar.gui.tracking.metatlism.food"),
            I18n.format("projectnublar.gui.tracking.metatlism.water")
        );
    }

    public static void encodeNBT(NBTTagCompound nbt, MetabolismInformation info) {
        nbt.setInteger("food", info.food);
        nbt.setInteger("max_food", info.maxFood);

        nbt.setInteger("water", info.water);
        nbt.setInteger("max_water", info.maxWater);
    }

    public static MetabolismInformation decodeNBT(NBTTagCompound nbt) {
        return new MetabolismInformation(
            nbt.getInteger("food"), nbt.getInteger("max_food"),
            nbt.getInteger("water"), nbt.getInteger("max_water")
        );
    }

    public static void encodeBuf(ByteBuf buf, MetabolismInformation info) {
        buf.writeInt(info.food);
        buf.writeInt(info.maxFood);
        buf.writeInt(info.water);
        buf.writeInt(info.maxWater);
    }

    public static MetabolismInformation decodeBuf(ByteBuf buf) {
        return new MetabolismInformation(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }
}
