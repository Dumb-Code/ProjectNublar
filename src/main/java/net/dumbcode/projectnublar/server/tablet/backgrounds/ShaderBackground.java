package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import net.dumbcode.dumblibrary.client.shader.GlslSandboxShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Getter
public class ShaderBackground implements TabletBackground {

    public static final String KEY = "shader";

    private GlslSandboxShader shader;
    private boolean needsUpdating;
    private String url = "";
    private ScreenSize size = ScreenSize.FIT_TO_GUI_LOW_RES;

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("url", this.url);
        nbt.setString("screen_size", this.size.name());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.setUrl(nbt.getString("url"));
        if(nbt.hasKey("screen_size", Constants.NBT.TAG_STRING)) {
            this.size = ScreenSize.valueOf(nbt.getString("screen_size"));
        }
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.url);
        buf.writeByte(this.size.ordinal());
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.setUrl(ByteBufUtils.readUTF8String(buf));
        this.size = ScreenSize.values()[buf.readByte() % ScreenSize.values().length];
    }

    public void setUrl(String url) {
        this.needsUpdating = !this.url.equals(url);
        this.url = url;
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        if(this.needsUpdating) {
            this.needsUpdating = false;
            this.dispose();
            this.shader = GlslSandboxShader.createShader(this.url);
        }

        if(this.shader != null) {
            float modifier = this.size.modifier.get();
            int shaderWidth = (int) (width * modifier);
            int shaderHeight = (int) (height * modifier);
            if(this.shader.getScreenWidth() != shaderWidth || this.shader.getScreenHeight() != shaderHeight) {
                this.shader.init(shaderWidth, shaderHeight);
            }
            this.shader.render(mouseX - x, mouseY - y);
            this.shader.startShader();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            this.shader.endShader();
        }
    }

    @Override
    public void dispose() {
        if(this.shader != null) {
            this.shader.dispose();
        }
    }

    public void setSize(@NonNull ScreenSize size) {
        this.size = size;
    }

    @Getter
    public enum ScreenSize {
        FIT_TO_SCREEN(() -> (float) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor()),
        FIT_TO_GUI(() -> 1F),
        FIT_TO_GUI_LOW_RES(() -> 1/2F),
        FIT_TO_GUI_LOWEST_RES(() -> 1/4F);
        private final String translationKey;
        private final Supplier<Float> modifier;

        ScreenSize(Supplier<Float> modifier) {
            this.translationKey = "projectnublar.gui.shader." + this.name().toLowerCase();
            this.modifier = modifier;
        }
    }
}
