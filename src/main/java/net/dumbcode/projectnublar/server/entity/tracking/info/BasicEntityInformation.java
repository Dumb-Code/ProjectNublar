package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Value
@EqualsAndHashCode(callSuper = true)
public class BasicEntityInformation extends TooltipInformation {

    public static final String KEY = "basic_entity_information";

    private static final int PADDING_AFTER_TEXT = 10;

    private static final int HEART_SIZE = 9;
    private static final int HEARTS_PER_LINE = 5;

    private final float health;
    private final float maxHealth;

    private final int hearts;
    private final int maxHearts;

    public BasicEntityInformation(float health, float maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;

        this.maxHearts = (int) Math.ceil(this.maxHealth / 2F);
        this.hearts = (int) Math.ceil(this.health / 2F);
    }

    @Override
    protected String getTypeName() {
        return KEY;
    }

    @Override
    protected List<String> getTooltipLines() {
        return Collections.singletonList(I18n.format("projectnublar.gui.tracking.health"));
    }

    @Nonnull
    @Override
    public Dimension getInfoDimensions() {
        Dimension d = super.getInfoDimensions();
        return new Dimension(d.width + PADDING_AFTER_TEXT + Math.min(this.maxHearts, HEARTS_PER_LINE) * HEART_SIZE, Math.max(d.height, HEART_SIZE * this.maxHearts / HEARTS_PER_LINE));
    }

    @Override
    public void renderInfo(int x, int y, int relativeMouseX, int relativeMouseY) {
        super.renderInfo(x, y, relativeMouseX, relativeMouseY);

        int startX = super.getInfoDimensions().width + PADDING_AFTER_TEXT;
        GuiScreen screen = Objects.requireNonNull(Minecraft.getMinecraft().currentScreen);
        Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int heart = 0; heart < this.maxHearts; heart++) {
            int heartX = HEART_SIZE * (heart % HEARTS_PER_LINE) - HEART_SIZE/2 + x + startX;
            int heartY = HEART_SIZE * (heart / HEARTS_PER_LINE) + y;

            screen.drawTexturedModalRect(heartX, heartY, 16, 0, HEART_SIZE, HEART_SIZE);

            if(heart*2 <= this.health) {
                if(heart*2 + 1 < this.health) {
                    screen.drawTexturedModalRect(heartX, heartY, 52, 0, HEART_SIZE, HEART_SIZE);
                } else {
                    screen.drawTexturedModalRect(heartX, heartY, 61, 0, HEART_SIZE, HEART_SIZE);
                }
            }
        }

        if(relativeMouseX >= startX && relativeMouseY != -1) {
            GuiUtils.drawHoveringText(ItemStack.EMPTY, Collections.singletonList((Math.round(this.health * 10) / 10F) + "/" + (Math.round(this.maxHealth * 10) / 10F)),
                x + relativeMouseX, y + relativeMouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, -1, FONT_RENDERER
            );
        }
    }

    public static void encodeNBT(NBTTagCompound nbt, BasicEntityInformation info) {
        nbt.setFloat("health", info.health);
        nbt.setFloat("max_health", info.maxHealth);
    }

    public static BasicEntityInformation decodeNBT(NBTTagCompound nbt) {
        return new BasicEntityInformation(
            nbt.getFloat("health"),
            nbt.getFloat("max_health")
        );
    }

    public static void encodeBuf(ByteBuf buf, BasicEntityInformation info) {
        buf.writeFloat(info.health);
        buf.writeFloat(info.maxHealth);
    }

    public static BasicEntityInformation decodeBuf(ByteBuf buf) {
        return new BasicEntityInformation(
            buf.readFloat(),
            buf.readFloat()
        );
    }
}
