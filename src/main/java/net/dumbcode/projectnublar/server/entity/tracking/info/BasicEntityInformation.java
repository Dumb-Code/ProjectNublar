package net.dumbcode.projectnublar.server.entity.tracking.info;

import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.RepeatingIconDisplay;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
public class BasicEntityInformation extends TooltipInformation {

    public static final String KEY = "basic_entity_information";

    private static final int PADDING_AFTER_TEXT = 10;

    private static final int HEART_SIZE = 9;

    private final RepeatingIconDisplay display;

    private float health;
    private float maxHealth;

    public BasicEntityInformation(float health, float maxHealth) {
        this.health = health;
        this.maxHealth = maxHealth;

        this.display = new RepeatingIconDisplay(health, maxHealth, HEART_SIZE, 5, 2, this::renderHeartAt);
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
        return new Dimension(d.width + PADDING_AFTER_TEXT + this.display.getWidth(), Math.max(d.height, this.display.getHeight()));
    }

    private void renderHeartAt(int x, int y, float size) {
        RenderUtils.draw256Texture(x, y, 16, 0, HEART_SIZE, HEART_SIZE);
        if(size > 0) {
            RenderUtils.draw256Texture(x, y, 52, 0, size * HEART_SIZE, HEART_SIZE);
        }
    }

    @Override
    public void renderInfo(int x, int y, int relativeMouseX, int relativeMouseY) {
        super.renderInfo(x, y, relativeMouseX, relativeMouseY);

        int startX = super.getInfoDimensions().width + PADDING_AFTER_TEXT;
        Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.display.render(x + startX, y);

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
