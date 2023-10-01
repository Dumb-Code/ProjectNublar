package net.dumbcode.projectnublar.server.entity.tracking.info;

import com.mojang.blaze3d.matrix.GuiGraphics;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.dumbcode.dumblibrary.client.RepeatingIconDisplay;
import net.dumbcode.projectnublar.server.entity.tracking.TooltipInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.GuiUtils;

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
        return Collections.singletonList(I18n.get("projectnublar.gui.tracking.health"));
    }

    @Nonnull
    @Override
    public Dimension getInfoDimensions() {
        Dimension d = super.getInfoDimensions();
        return new Dimension(d.width + PADDING_AFTER_TEXT + this.display.getWidth(), Math.max(d.height, this.display.getHeight()));
    }

    private void renderHeartAt(GuiGraphics stack, int x, int y, float size) {
        AbstractGui.stack.blit(x, y, 0, 16, 0, HEART_SIZE, HEART_SIZE, 256, 256);
        if(size > 0) {
            AbstractGui.stack.blit(x, y, 52, 16, 0, (int) (size * HEART_SIZE), HEART_SIZE, 256, 256);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInfo(GuiGraphics stack, int x, int y, int relativeMouseX, int relativeMouseY) {
        super.renderInfo(stack, x, y, relativeMouseX, relativeMouseY);

        int startX = super.getInfoDimensions().width + PADDING_AFTER_TEXT;
        Minecraft.getInstance().textureManager.bind(AbstractGui.GUI_ICONS_LOCATION);

        this.display.render(stack, x + startX, y);

        if(relativeMouseX >= startX && relativeMouseY != -1) {
            GuiUtils.drawHoveringText(ItemStack.EMPTY, stack, Collections.singletonList(Component.literal((Math.round(this.health * 10) / 10F) + "/" + (Math.round(this.maxHealth * 10) / 10F))),
                x + relativeMouseX, y + relativeMouseY, Integer.MAX_VALUE, Integer.MAX_VALUE, -1, Minecraft.getInstance().font
            );
        }
    }

    public static void encodeNBT(CompoundNBT nbt, BasicEntityInformation info) {
        nbt.putFloat("health", info.health);
        nbt.putFloat("max_health", info.maxHealth);
    }

    public static BasicEntityInformation decodeNBT(CompoundNBT nbt) {
        return new BasicEntityInformation(
            nbt.getFloat("health"),
            nbt.getFloat("max_health")
        );
    }

    public static void encodeBuf(PacketBuffer buf, BasicEntityInformation info) {
        buf.writeFloat(info.health);
        buf.writeFloat(info.maxHealth);
    }

    public static BasicEntityInformation decodeBuf(PacketBuffer buf) {
        return new BasicEntityInformation(
            buf.readFloat(),
            buf.readFloat()
        );
    }
}
